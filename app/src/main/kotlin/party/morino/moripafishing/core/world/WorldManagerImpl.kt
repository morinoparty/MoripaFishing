package party.morino.moripafishing.core.world

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
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
import party.morino.moripafishing.core.world.biome.ConstBiomeGenerator

class WorldManagerImpl : WorldManager, KoinComponent {
    private val plugin : MoripaFishing by inject()
    private val configManager: ConfigManager by inject()
    private val pluginDirectory : PluginDirectory by inject()
    private val worldConfig : WorldConfig
        get() = configManager.getConfig().world

    private lateinit var worldIdList : Set<FishingWorldId>

    init{
        loadWorldIds()
        worldIdList.forEach { fishingWorldId ->
            val res = createWorld(fishingWorldId)
            if (res) {
                plugin.logger.info("World $fishingWorldId created")
            } else {
                plugin.logger.info("World $fishingWorldId is found")
            }
        }
        plugin.logger.info("World created!")
    }

    private fun loadWorldIds() {
        val worldDirectory = pluginDirectory.getWorldDirectory()
        if (!worldDirectory.exists()) {
            worldDirectory.mkdirs()
        }
        worldIdList = (pluginDirectory.getWorldDirectory().listFiles()?.mapNotNull { file ->
            if (file.name.endsWith(".json")) {
                val worldId = file.nameWithoutExtension
                FishingWorldId(worldId)
            } else {
                null
            }
        } ?: emptyList()).toSet()
    }


    override fun getDefaultWorldId(): FishingWorldId {
       return worldConfig.defaultId
    }

    override fun getWorldIdList(): List<FishingWorldId> {
        return worldIdList.toList()
    }

    override fun getWorldDetails(fishingWorldId: FishingWorldId): WorldDetailConfig? {
        val file = pluginDirectory.getWorldDirectory().resolve("${fishingWorldId.value}.json")
        if (!file.exists()) {
            return null
        }
        val worldDetailConfig = Json.decodeFromStream<WorldDetailConfig>(file.inputStream())
        return worldDetailConfig
    }

    override fun getWorld(fishingWorldId: FishingWorldId): FishingWorld {
        return FishingWorldImpl(fishingWorldId)
    }

    override fun createWorld(fishingWorldId: FishingWorldId): Boolean {
        val worldGenerator = worldConfig.defaultWorldGenerator
        val biomeProvider = worldConfig.defaultWorldBiome
        return createWorld(fishingWorldId, worldGenerator, biomeProvider)
    }

    override fun createWorld(fishingWorldId: FishingWorldId, generator: String?, biome: String?): Boolean {
        if (Bukkit.getWorld(fishingWorldId.value)!=null) {
            return false
        }
        val default = listOf("world", "world_nether", "world_the_end")
        if (default.contains(fishingWorldId.value)) {
            plugin.logger.warning("World name is not allowed: ${fishingWorldId.value}")
            return false
        }
        val namespacedKey = NamespacedKey(plugin, fishingWorldId.value)
        plugin.logger.info("Creating world ${fishingWorldId.value}")

        val file = pluginDirectory.getWorldDirectory().resolve("${fishingWorldId.value}.json")
        if (!file.exists()) {
            val worldDetailConfig = WorldDetailConfig(id = fishingWorldId, name = fishingWorldId.value )
            file.createNewFile()
            file.writeText(Json{
                prettyPrint = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            }.encodeToString(WorldDetailConfig.serializer(), worldDetailConfig))
        }

        val biomeProvider = biome?.let { ConstBiomeGenerator(biome) }
        val creator = WorldCreator(namespacedKey).generator(generator
            ?: worldConfig.defaultWorldGenerator).biomeProvider(biomeProvider)
        val world = Bukkit.createWorld(creator)
        if (world==null) {
            plugin.logger.warning("Failed to create world ${fishingWorldId.value}")
            return false
        }
        plugin.logger.info("World ${world.name}")
        worldIdList.plus(world)
        return true
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
}