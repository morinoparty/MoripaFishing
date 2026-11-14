package party.morino.moripafishing.addons.chum.effect

import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import kotlin.math.hypot

/**
 * 撒き餌の効果の対象。
 */
sealed interface ChumTarget {
    /**
     * 抽選がこの対象に含まれるかを返す。
     *
     * @param angler 釣りをしたつりびと
     * @param location 釣り針の位置 (`/chum odds` ではつりびとの位置)
     */
    fun covers(
        angler: AnglerId,
        location: LocationData,
    ): Boolean

    /** 使用したつりびと本人。 */
    data class Angler(
        val angler: AnglerId,
    ) : ChumTarget {
        override fun covers(
            angler: AnglerId,
            location: LocationData,
        ): Boolean = angler == this.angler
    }

    /** 釣りワールド [world] の、中心 ([x], [z]) から水平距離 [radius] 以内。 */
    data class Area(
        val world: FishingWorldId,
        val x: Double,
        val z: Double,
        val radius: Double,
    ) : ChumTarget {
        override fun covers(
            angler: AnglerId,
            location: LocationData,
        ): Boolean = location.worldId == world && hypot(location.x - x, location.z - z) <= radius
    }

    /** 釣りワールド全体。 */
    data class World(
        val world: FishingWorldId,
    ) : ChumTarget {
        override fun covers(
            angler: AnglerId,
            location: LocationData,
        ): Boolean = location.worldId == world
    }
}
