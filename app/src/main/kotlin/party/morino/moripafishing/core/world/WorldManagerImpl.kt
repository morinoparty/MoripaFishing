package party.morino.moripafishing.core.world

import jdk.internal.net.http.frame.Http2Frame.asString
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
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.core.world.WorldManager
import javax.naming.Name
import kotlin.getValue

class WorldManagerImpl : WorldManager, KoinComponent {
    private val plugin : MoripaFishing by inject()
    private val configManager: ConfigManager by inject()
    private val worldConfig : WorldConfig
        get() = configManager.getConfig().world

    init{
        val world = worldConfig.list.map {
            it.id
        }
        world.forEach { fishingWorldId ->
            val namespacedKey = NamespacedKey(plugin, fishingWorldId.value)
            if(Bukkit.getWorld(namespacedKey) != null){
                return@forEach
            }
            plugin.logger.info("Creating world ${namespacedKey.asString()}")
            val worldDetailConfig = worldConfig.list.find { it.id == fishingWorldId } ?: run {
                plugin.logger.warning("WorldDetailConfig not found for ${namespacedKey.asString()}")
                return@forEach
            }
            val worldGenerator = worldDetailConfig.worldGenerator
            val creator = WorldCreator(namespacedKey).generator(worldGenerator)
            val world = Bukkit.createWorld(creator)
            if(world == null){
                plugin.logger.warning("Failed to create world ${namespacedKey.asString()}")
                return@forEach
            }
            plugin.logger.info("World ${world.name}")
        }
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

}