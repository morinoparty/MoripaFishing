package party.morino.moripafishing.event.fishing

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.Fish

/**
 * 釣り人が魚を釣った際に発火するイベント
 * このイベントはキャンセル可能です。キャンセルされた場合、魚の記録が行われません。
 */
class FishCaughtEvent(
    private val player: Angler,
    private val fish: Fish,
) : Event(), Cancellable {
    private var isCancelled: Boolean = false

    /**
     * 釣り人を取得する
     * @return 釣り人
     */
    fun getPlayer(): Angler {
        return player
    }

    /**
     * 釣った魚を取得する
     * @return 釣った魚
     */
    fun getFish(): Fish {
        return fish
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
}
