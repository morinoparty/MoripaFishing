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
import party.morino.moripafishing.event.world.FishingWorldCreateEvent
import party.morino.moripafishing.event.world.FishingWorldDeleteEvent
import party.morino.moripafishing.event.world.FishingWorldDeletedEvent
import party.morino.moripafishing.event.world.FishingWorldLoadEvent
import party.morino.moripafishing.event.world.FishingWorldUnloadEvent
import party.morino.moripafishing.integrations.worldlifecycle.api.GeneratorData
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
                val instance = FishingWorldImpl(world)
                worldList.add(instance)
                instance.updateState()
                FishingWorldLoadEvent(world).callEvent()
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

    override fun hasWorld(fishingWorldId: FishingWorldId): Boolean = fishingWorldId in worldIdList

    override fun getWorld(fishingWorldId: FishingWorldId): FishingWorld? {
        worldList.find { it.getId() == fishingWorldId }?.let { return it }
        // 登録済み (設定ファイルが存在する) かつ Bukkit ワールドがロード済みの場合のみ遅延構築する。
        // 未登録の ID に対してワールドを捏造しない。
        if (fishingWorldId !in worldIdList) return null
        if (Bukkit.getWorld(fishingWorldId.value) == null) return null
        val world = FishingWorldImpl(fishingWorldId)
        worldList.add(world)
        return world
    }

    override fun getWorlds(): List<FishingWorld> = worldIdList.toList().mapNotNull { getWorld(it) }

    override fun getGeneratorIds(): List<String> = plugin.getWorldLifecycleProvider()?.listGenerators()?.map { it.id } ?: emptyList()

    override fun createWorld(
        fishingWorldId: FishingWorldId,
        generatorId: String,
    ): Boolean {
        val provider =
            plugin.getWorldLifecycleProvider() ?: run {
                plugin.logger.warning(
                    "createWorld(${fishingWorldId.value}) requires the WorldLifecycle integration but it is not installed.",
                )
                return false
            }
        val generator =
            provider.getGenerator(generatorId) ?: run {
                plugin.logger.warning(
                    "Generator '$generatorId' was not found in the WorldLifecycle integration.",
                )
                return false
            }
        return createWorld(fishingWorldId, generator)
    }

    /**
     * 指定されたジェネレータデータで Bukkit ワールドを作成する内部実装。
     * Integration 未導入時は warning を出して `false` を返す。
     */
    private fun createWorld(
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

        // 起動時 (initializeWorlds) の既存ワールド再ロードでは worldIdList に既に含まれる。
        // 真に新規作成された場合のみ FishingWorldCreateEvent を発火するため、事前に判定しておく。
        val isNewWorld = fishingWorldId !in worldIdList

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

        val created = provider.createBukkitWorld(fishingWorldId.value, generatorData)
        if (!created) {
            return false
        }
        val instance = FishingWorldImpl(fishingWorldId)
        worldList.add(instance)
        instance.updateState()
        worldIdList.add(fishingWorldId)
        plugin.logger.info("Current world list: ${worldList.map { it.getId().value }}")
        if (isNewWorld) {
            FishingWorldCreateEvent(fishingWorldId).callEvent()
        }
        FishingWorldLoadEvent(fishingWorldId).callEvent()
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
                    "Generator '${detailConfig.generator}' was not found in the WorldLifecycle integration.",
                )
                return false
            }
        return createWorld(fishingWorldId, generator)
    }

    override fun deleteWorld(fishingWorldId: FishingWorldId): Boolean {
        val world = Bukkit.getWorld(fishingWorldId.value) ?: return false
        val event = FishingWorldDeleteEvent(fishingWorldId)
        if (!event.callEvent()) {
            plugin.logger.info("deleteWorld(${fishingWorldId.value}) was cancelled by an event handler.")
            return false
        }
        if (!Bukkit.unloadWorld(world, false)) {
            plugin.logger.warning(
                "deleteWorld(${fishingWorldId.value}) aborted: Bukkit.unloadWorld failed (players still in the world?).",
            )
            return false
        }
        val worldFile = pluginDirectory.getWorldDirectory().resolve("${fishingWorldId.value}.json")
        if (worldFile.exists()) {
            worldFile.delete()
        }
        // 破棄前に天候プロバイダーの Listener 等を解放する
        worldList
            .filter { it.getId() == fishingWorldId }
            .forEach { (it as? FishingWorldImpl)?.disposeWeatherProvider() }
        worldList.removeIf { it.getId() == fishingWorldId }
        worldIdList.remove(fishingWorldId)
        FishingWorldUnloadEvent(fishingWorldId).callEvent()
        FishingWorldDeletedEvent(fishingWorldId).callEvent()
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
