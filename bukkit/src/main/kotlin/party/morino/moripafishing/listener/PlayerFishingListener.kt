import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import org.koin.core.component.KoinComponent

class PlayerFishingListener : Listener, KoinComponent {
    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        val player = event.player
        val state = event.state

        when (state) {
            PlayerFishEvent.State.CAUGHT_FISH -> {
                val fish = event.
            }
            else -> {
                return
            }        }
    }
}
