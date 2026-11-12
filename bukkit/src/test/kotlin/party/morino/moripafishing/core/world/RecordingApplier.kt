package party.morino.moripafishing.core.world

import party.morino.moripafishing.api.core.world.spi.WorldState
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier
import party.morino.moripafishing.api.model.world.FishingWorldId

/** 受け取った [WorldState] を記録する [WorldStateApplier]。 */
internal class RecordingApplier : WorldStateApplier {
    val states = mutableListOf<WorldState>()

    override fun apply(state: WorldState) {
        states += state
    }

    fun last(id: String): WorldState? = states.lastOrNull { it.worldId == FishingWorldId(id) }
}
