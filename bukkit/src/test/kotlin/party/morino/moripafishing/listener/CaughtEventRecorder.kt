package party.morino.moripafishing.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import party.morino.moripafishing.api.event.fishing.AnglerFishCaughtEvent
import party.morino.moripafishing.api.event.fishing.AnglerFishCaughtResultEvent
import party.morino.moripafishing.api.model.fish.CaughtFish

/**
 * [AnglerFishCaughtEvent] に [onCaught] を適用し、[AnglerFishCaughtResultEvent] の釣果を記録する。
 */
internal class CaughtEventRecorder(
    private val onCaught: (AnglerFishCaughtEvent) -> Unit,
) : Listener {
    val results = mutableListOf<CaughtFish>()

    @EventHandler
    fun onCaught(event: AnglerFishCaughtEvent) = onCaught.invoke(event)

    @EventHandler
    fun onResult(event: AnglerFishCaughtResultEvent) {
        results += event.getCaughtFish()
    }
}
