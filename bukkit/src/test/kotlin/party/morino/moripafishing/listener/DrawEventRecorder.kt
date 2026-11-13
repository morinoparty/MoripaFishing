package party.morino.moripafishing.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import party.morino.moripafishing.api.event.fishing.AnglerFishDrawEvent

/**
 * [AnglerFishDrawEvent] に [onDraw] を適用して記録する。
 */
internal class DrawEventRecorder(
    private val onDraw: (AnglerFishDrawEvent) -> Unit,
) : Listener {
    val events = mutableListOf<AnglerFishDrawEvent>()

    @EventHandler
    fun onDraw(event: AnglerFishDrawEvent) {
        events += event
        onDraw.invoke(event)
    }
}
