package party.morino.moripafishing.core.world

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.apache.commons.lang3.StringUtils.endsWith
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World
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

    private lateinit var worldIdList : List<FishingWorldId>

    init{
        loadWorldIds()
        worldIdList.forEach { fishingWorldId ->
            val res = createWorld(fishingWorldId)
            if (res!=null) {
                plugin.logger.info("World $fishingWorldId created")
            } else {
                plugin.logger.info("World $fishingWorldId is found")
            }
        }
        plugin.logger.info("World created!")
        val world = worldIdList.map { getWorld(it) }.forEach { it.refreshSetting() }
    }

    private fun loadWorldIds() {
        val worldDirectory = pluginDirectory.getWorldDirectory()
        if (!worldDirectory.exists()) {
            worldDirectory.mkdirs()
        }
        worldIdList = pluginDirectory.getWorldDirectory().listFiles()?.mapNotNull { file ->
            if (file.name.endsWith(".json")) {
                val worldId = file.nameWithoutExtension
                FishingWorldId(worldId)
            } else {
                null
            }
        } ?: emptyList()
    }


    override fun getDefaultWorldId(): FishingWorldId {
       return worldConfig.defaultId
    }

    override fun getWorldIdList(): List<FishingWorldId> {
        return worldIdList
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

    override fun createWorld(fishingWorldId: FishingWorldId): FishingWorld? {
        val minecraftWorldName = listOf("world", "world_nether", "world_end")

        val namespacedKey = if (minecraftWorldName.contains(fishingWorldId.value)) {
            NamespacedKey.minecraft(fishingWorldId.value)
        } else {
            NamespacedKey(plugin, fishingWorldId.value)
        }
        if (Bukkit.getWorld(namespacedKey)!=null) {
            return FishingWorldImpl(fishingWorldId)
        }
        plugin.logger.info("Creating world ${fishingWorldId.value}")

        val worldGenerator = worldConfig.defaultWorldGenerator
        val biomeProvider = worldConfig.defaultWorldBiome?.let { ConstBiomeGenerator(it) }
        val creator = WorldCreator(namespacedKey).generator(worldGenerator).biomeProvider(biomeProvider)
        val world = Bukkit.createWorld(creator)
        if (world==null) {
            plugin.logger.warning("Failed to create world ${fishingWorldId.value}")
            return null
        }
        plugin.logger.info("World ${world.name}")
        return FishingWorldImpl(fishingWorldId)
    }

}