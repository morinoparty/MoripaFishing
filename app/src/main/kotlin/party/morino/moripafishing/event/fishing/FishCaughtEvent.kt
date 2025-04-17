//TODO implement this event
package party.morino.moripafishing.event.fishing

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.Fish


class FishCaughtEvent(
    private val player: Angler,
    private val fish: Fish,
) : Event() , Cancellable {
    private val handlers = HandlerList()
    private var isCancelled: Boolean

    init {
        this.isCancelled = false
    }

    override fun getHandlers(): HandlerList {
        return handlers
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }
}

