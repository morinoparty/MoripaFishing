package party.morino.moripafishing.core.angler

import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.LocationData

/**
 * [Angler] の実装。値は呼び出しのたびに Bukkit から取得する。
 *
 * @property id 釣り人の ID
 * @property worlds 釣り人がいる釣りワールドの解決に使う
 */
internal class AnglerImpl(
    private val id: AnglerId,
    private val worlds: WorldManager,
) : Angler {
    override fun getId(): AnglerId = id

    override fun getName(): String = Bukkit.getOfflinePlayer(id.value).name ?: id.value.toString()

    override fun getWorld(): FishingWorld? = player()?.let { worlds.getWorldByKey(it.world.key) }

    override fun getLocation(): LocationData? {
        val player = player() ?: return null
        val world = worlds.getWorldByKey(player.world.key) ?: return null
        val location = player.location
        return LocationData(world.getId(), location.x, location.y, location.z, location.yaw.toDouble(), location.pitch.toDouble())
    }

    override fun getAudience(): Audience = player() ?: Audience.empty()

    private fun player(): Player? = Bukkit.getPlayer(id.value)
}
