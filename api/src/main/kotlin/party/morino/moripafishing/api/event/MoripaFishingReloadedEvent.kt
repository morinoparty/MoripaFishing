package party.morino.moripafishing.api.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * `/mf reload` で設定・ワールド・レアリティ・魚・翻訳の再読み込みがすべて完了した後に発火する。
 *
 * メインスレッドで発火するポストイベントであり、キャンセルはできません。
 * 設定由来の値をキャッシュしているアドオンはこのイベントで再取得してください。
 */
class MoripaFishingReloadedEvent : Event() {
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

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
