package party.morino.moripafishing.event.config

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * このイベントは、`/mf reload` などによる設定の再読み込みが完了した後に発生します。
 *
 * 設定・ワールド・レアリティ・魚・翻訳の再読み込みがすべて完了してから発火する
 * ポストイベントであり、キャンセルはできません。
 * 設定由来の値をキャッシュしているアドオンはこのイベントで再取得してください。
 */
class ConfigReloadedEvent : Event() {
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
