package party.morino.moripafishing.core.world

import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.config.world.WorldConfig
import party.morino.moripafishing.api.config.world.WorldDetailConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.generator.GeneratorData
import party.morino.moripafishing.utils.Utils

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
class WorldManagerImpl :
    WorldManager,
    KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val configManager: ConfigManager by inject()
    private val pluginDirectory: PluginDirectory by inject()
    private val worldConfig: WorldConfig
        get() = configManager.getConfig().world

    private val worldList: MutableSet<FishingWorld> = mutableSetOf()
    private lateinit var worldIdList: MutableSet<FishingWorldId>

    init {
        loadWorlds()
        plugin.logger.info("loaded worlds! ${worldIdList.map { it.value }}")
    }

    private fun loadWorlds() {
        val worldDirectory = pluginDirectory.getWorldDirectory()
        if (!worldDirectory.exists()) {
            worldDirectory.mkdirs()
        }
        worldIdList =
            pluginDirectory
                .getWorldDirectory()
                .listFiles()
                .filter { it.name.endsWith(".json") }
                .map { FishingWorldId(it.nameWithoutExtension) }
                .toMutableSet()
    }

    override fun initializeWorlds() {
        worldIdList.toList().forEach { world ->
            // Bukkit ワールドが既に存在する場合は FishingWorldImpl を構築して updateState するのみ。
            // 存在しない場合は createWorld で生成する (WorldLifecycle integration が必要)。
            if (Bukkit.getWorld(world.value) != null) {
                plugin.logger.info("World is found! Skipping ${world.value}")
                worldList.add(FishingWorldImpl(world))
                worldList.find { it.getId() == world }?.updateState()
                return@forEach
            }
            if (createWorld(world)) {
                plugin.logger.info("World created! ${world.value}")
            } else {
                plugin.logger.warning(
                    "Skipping ${world.value}: Bukkit world is not loaded and creation failed " +
                        "(WorldLifecycle integration missing or generator invalid).",
                )
            }
        }
    }

    override fun getDefaultWorldId(): FishingWorldId = worldConfig.defaultId

    override fun getWorldIdList(): List<FishingWorldId> = worldIdList.toList()

    override fun getWorld(fishingWorldId: FishingWorldId): FishingWorld =
        worldList.find { it.getId() == fishingWorldId } ?: run {
            val world = FishingWorldImpl(fishingWorldId)
            worldList.add(world)
            world
        }

    override fun createWorld(
        fishingWorldId: FishingWorldId,
        generatorData: GeneratorData,
    ): Boolean {
        val provider =
            plugin.getWorldLifecycleProvider() ?: run {
                plugin.logger.warning(
                    "createWorld(${fishingWorldId.value}) requires the WorldLifecycle integration but it is not installed.",
                )
                return false
            }

        plugin.logger.info("Creating or Loading world ${fishingWorldId.value}")

        val file = pluginDirectory.getWorldDirectory().resolve("${fishingWorldId.value}.json")
        if (!file.exists()) {
            val worldDetailConfig =
                WorldDetailConfig(
                    id = fishingWorldId,
                    name =
                        mapOf(
                            configManager.getConfig().defaultLocale to fishingWorldId.value,
                        ),
                )
            file.createNewFile()
            file.writeText(
                Utils.json.encodeToString(WorldDetailConfig.serializer(), worldDetailConfig),
            )
        }

        val created = provider.createBukkitWorld(fishingWorldId, generatorData)
        if (!created) {
            return false
        }
        val instance = FishingWorldImpl(fishingWorldId)
        worldList.add(instance)
        instance.updateState()
        worldIdList.add(fishingWorldId)
        plugin.logger.info("Current world list: ${worldList.map { it.getId().value }}")
        return true
    }

    override fun createWorld(fishingWorldId: FishingWorldId): Boolean {
        val provider =
            plugin.getWorldLifecycleProvider() ?: run {
                plugin.logger.warning(
                    "createWorld(${fishingWorldId.value}) requires the WorldLifecycle integration but it is not installed.",
                )
                return false
            }
        val detailConfig = getWorldDetailConfig(fishingWorldId)
        val generator =
            provider.getGenerator(detailConfig.generator) ?: run {
                plugin.logger.warning(
                    "Generator '${detailConfig.generator.value}' was not found in the WorldLifecycle integration.",
                )
                return false
            }
        return createWorld(fishingWorldId, generator)
    }

    override fun deleteWorld(fishingWorldId: FishingWorldId): Boolean {
        val world = Bukkit.getWorld(fishingWorldId.value) ?: return false
        Bukkit.unloadWorld(world, false)
        val worldFile = pluginDirectory.getWorldDirectory().resolve("${fishingWorldId.value}.json")
        if (worldFile.exists()) {
            worldFile.delete()
        }
        return true
    }

    fun getWorldDetailConfig(fishingWorldId: FishingWorldId): WorldDetailConfig {
        val file = pluginDirectory.getWorldDirectory().resolve("${fishingWorldId.value}.json")
        if (!file.exists()) {
            return WorldDetailConfig(id = fishingWorldId, name = mapOf(configManager.getConfig().defaultLocale to fishingWorldId.value))
        }
        return Utils.json.decodeFromString(
            WorldDetailConfig.serializer(),
            file.readText(),
        )
    }
}
