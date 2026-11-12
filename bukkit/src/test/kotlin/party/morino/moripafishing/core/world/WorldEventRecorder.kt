package party.morino.moripafishing.core.world

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import party.morino.moripafishing.api.event.world.FishingWorldCreateEvent
import party.morino.moripafishing.api.event.world.FishingWorldDeleteEvent
import party.morino.moripafishing.api.event.world.FishingWorldDeletedEvent
import party.morino.moripafishing.api.event.world.FishingWorldLoadEvent
import party.morino.moripafishing.api.event.world.FishingWorldUnloadEvent
import party.morino.moripafishing.api.event.world.FishingWorldWeatherChangeEvent

/** 釣りワールドのイベントを発火順に記録する。 */
internal class WorldEventRecorder : Listener {
    val events = mutableListOf<String>()

    @EventHandler
    fun onLoad(event: FishingWorldLoadEvent) {
        events += "load:${event.getWorldId().value}"
    }

    @EventHandler
    fun onUnload(event: FishingWorldUnloadEvent) {
        events += "unload:${event.getWorldId().value}"
    }

    @EventHandler
    fun onCreate(event: FishingWorldCreateEvent) {
        events += "create:${event.getWorldId().value}"
    }

    @EventHandler
    fun onDelete(event: FishingWorldDeleteEvent) {
        events += "delete:${event.getWorldId().value}"
    }

    @EventHandler
    fun onDeleted(event: FishingWorldDeletedEvent) {
        events += "deleted:${event.getWorldId().value}"
    }

    @EventHandler
    fun onWeatherChange(event: FishingWorldWeatherChangeEvent) {
        events += "weather:${event.getWorldId().value}:${event.getOldWeather()}->${event.getNewWeather()}"
    }
}
