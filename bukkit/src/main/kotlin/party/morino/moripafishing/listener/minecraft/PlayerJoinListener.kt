package party.morino.moripafishing.listener.minecraft

import org.bukkit.Bukkit
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
        val defaultWorld = Bukkit.getWorld(worldManager.getDefaultWorldId().value) ?: return
        player.teleport(defaultWorld.spawnLocation)
    }
}
