package party.morino.moripafishing.listener.minecraft

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.world.WorldManager

class PlayerJoinListener : Listener, KoinComponent {
    private val worldManager: WorldManager by inject()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val defaultWorldId = worldManager.getDefaultWorldId()
        val fishingWorld = worldManager.getWorld(defaultWorldId)
        val defaultWorld = Bukkit.getWorld(defaultWorldId.value) ?: return
        val spawnLocation = fishingWorld.getWorldDetails().spawnLocationData
        val location = Location(defaultWorld, spawnLocation.x, spawnLocation.y, spawnLocation.z,
            spawnLocation.yaw.toFloat(), spawnLocation.pitch.toFloat())
        player.teleport(location)
    }
}
