package party.morino.moripafishing.core.world

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.WorldConfig
import party.morino.moripafishing.api.config.WorldDetailConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.core.world.biome.ConstBiomeGenerator

class WorldManagerImpl : WorldManager, KoinComponent {
    private val plugin : MoripaFishing by inject()
    private val configManager: ConfigManager by inject()
    private val worldConfig : WorldConfig
        get() = configManager.getConfig().world

    init{
        val worldIds = worldConfig.list.map {
            it.id
        }
        worldIds.forEach { fishingWorldId ->
            val res = createWorld(fishingWorldId)
            if (res!=null) {
                plugin.logger.info("World $fishingWorldId created")
            } else {
                plugin.logger.info("World $fishingWorldId is found")
            }
        }
        plugin.logger.info("World created!")
        val world = worldIds.map { getWorld(it) }.forEach { it.refreshSetting() }

    }


    override fun getDefaultWorldId(): FishingWorldId {
       return worldConfig.defaultId
    }

    override fun getWorldIdList(): List<FishingWorldId> {
        return worldConfig.list.map { it.id }
    }

    override fun getWorldDetails(fishingWorldId: FishingWorldId): WorldDetailConfig? {
        return worldConfig.list.find { it.id == fishingWorldId }
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