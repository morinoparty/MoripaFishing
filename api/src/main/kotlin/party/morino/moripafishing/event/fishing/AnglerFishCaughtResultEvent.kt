package party.morino.moripafishing.event.fishing

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.model.fish.CaughtFish

/**
 * このイベントは、釣り上げ処理が完了した後に発生します。
 *
 * [AnglerFishCaughtEvent] がキャンセルされず、釣果のアイテム化まで完了した場合にのみ発火する
 * モニター用途のポストイベントであり、キャンセルはできません。
 * 通知・記録などの副作用はこちらを購読してください。
 *
 * @param angler 釣りをした釣り人
 * @param caughtFish 確定した釣果
 */
class AnglerFishCaughtResultEvent(
    private val angler: Angler,
    private val caughtFish: CaughtFish,
) : Event() {
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

    fun getAngler(): Angler = angler

    fun getCaughtFish(): CaughtFish = caughtFish

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
