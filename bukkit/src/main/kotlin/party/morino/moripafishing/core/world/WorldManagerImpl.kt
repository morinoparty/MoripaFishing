package party.morino.moripafishing.core.world

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.bukkit.WorldType
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
import party.morino.moripafishing.core.world.biome.ConstBiomeGenerator
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
        worldIdList.forEach { world ->
            val res = createWorld(world)
            if (res == true) {
                plugin.logger.info("World created! ${world.value}")
            } else {
                plugin.logger.info("World is found! Skipping ${world.value}")
                worldList.add(FishingWorldImpl(world))
                worldList.find { it.getId() == world }?.updateState()
                worldIdList.add(world)
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
        if (Bukkit.getWorld(fishingWorldId.value) != null) {
            return false
        }
        val default = listOf("world", "world_nether", "world_the_end")
        if (default.contains(fishingWorldId.value)) {
            plugin.logger.warning("World name is not allowed: ${fishingWorldId.value}")
            return false
        }
        val namespacedKey = NamespacedKey(plugin, fishingWorldId.value)

        plugin.logger.info("Creating or Loading world ${fishingWorldId.value}")

        val file = pluginDirectory.getWorldDirectory().resolve("${fishingWorldId.value}.json")
        if (!file.exists()) {
            val worldDetailConfig = WorldDetailConfig(id = fishingWorldId, name = fishingWorldId.value)
            file.createNewFile()
            file.writeText(
                Utils.json.encodeToString(WorldDetailConfig.serializer(), worldDetailConfig),
            )
        }

        val biomeProvider = generatorData.biomeProvider?.let { ConstBiomeGenerator(it) }
        val creator =
            WorldCreator(namespacedKey)
                .generator(generatorData.generator)
                .let { creator ->
                    generatorData.generatorSetting?.let { it1 -> creator.generatorSettings(it1) } ?: creator
                }.let { creator ->
                    generatorData.type?.let { type -> creator.type(WorldType.valueOf(type)) } ?: creator
                }.biomeProvider(biomeProvider)
        val world = Bukkit.createWorld(creator)
        if (world == null) {
            plugin.logger.warning("Failed to create world ${fishingWorldId.value}")
            return false
        }
        // 世界を初期化
        plugin.logger.info("World ${world.name}")
        val instance = FishingWorldImpl(fishingWorldId)
        worldList.add(instance)
        instance.updateState()
        worldIdList.add(fishingWorldId)
        plugin.logger.info("Current world list: ${worldList.map { it.getId().value }}")
        return true
    }

    override fun createWorld(fishingWorldId: FishingWorldId): Boolean {
        val detailConfig = getWorldDetailConfig(fishingWorldId)
        val generator = detailConfig.generator.toGeneratorData()
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
            return WorldDetailConfig(id = fishingWorldId, name = fishingWorldId.value)
        }
        return Utils.json.decodeFromString(
            WorldDetailConfig.serializer(),
            file.readText(),
        )
    }
}
