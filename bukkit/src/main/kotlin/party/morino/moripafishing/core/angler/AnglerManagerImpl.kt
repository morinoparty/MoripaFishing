package party.morino.moripafishing.core.angler

import org.bukkit.Bukkit
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId

/**
 * [AnglerManager] の実装。釣り人は状態を持たないため、取得のたびに作る。
 *
 * @property worlds 釣り人がいる釣りワールドの解決に使う
 */
internal class AnglerManagerImpl(
    private val worlds: WorldManager,
) : AnglerManager {
    override fun getAngler(id: AnglerId): Angler = AnglerImpl(id, worlds)

    override fun getOnlineAnglers(): List<Angler> = Bukkit.getOnlinePlayers().map { getAngler(AnglerId(it.uniqueId)) }
}
