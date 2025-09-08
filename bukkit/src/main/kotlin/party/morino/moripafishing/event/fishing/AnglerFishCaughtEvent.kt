package party.morino.moripafishing.event.fishing

import io.papermc.paper.event.server.ServerResourcesReloadedEvent.HANDLER_LIST
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.model.fish.CaughtFish

/**
 * このイベントは、釣り人が魚を釣ったときに発生します。
 *
 * @param angler 釣りをした釣り人
 * @param caughtFish 釣れた魚
 */
class AnglerFishCaughtEvent(
    private val angler: Angler,
    private val caughtFish: CaughtFish,
) : Event(),
    Cancellable {
    companion object {
        @JvmStatic
        private val HANDLER_LIST: HandlerList = HandlerList()

        /**
         * イベントのハンドラリストを取得します。
         * 必須メソッドです。
         * @return イベントのハンドラリスト
         */
        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    private var isCancelled: Boolean

    init {
        this.isCancelled = false
    }

    override fun isCancelled(): Boolean = isCancelled

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }

    fun getAngler(): Angler = angler

    fun getCaughtFish(): CaughtFish = caughtFish

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
