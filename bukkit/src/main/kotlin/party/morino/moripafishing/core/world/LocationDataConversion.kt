package party.morino.moripafishing.core.world

import org.bukkit.Location
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData

/**
 * Bukkit の位置を、釣りワールドの位置として [LocationData] に変換する。
 *
 * @param world 位置がある釣りワールドの ID
 */
internal fun Location.toLocationData(world: FishingWorldId): LocationData = LocationData(world, x, y, z, yaw.toDouble(), pitch.toDouble())
