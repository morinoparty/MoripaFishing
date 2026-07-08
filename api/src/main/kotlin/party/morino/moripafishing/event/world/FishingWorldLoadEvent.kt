package party.morino.moripafishing.event.world

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * このイベントは、釣りワールドがロードされ利用可能になった後に発生します。
 *
 * サーバー起動時の既存ワールドの読み込みと、ワールド新規作成の両方で発火します。
 * ポストイベントであり、キャンセルはできません。
 *
 * @param worldId ロードされた釣りワールドのID
 */
class FishingWorldLoadEvent(
    private val worldId: FishingWorldId,
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

    fun getWorldId(): FishingWorldId = worldId

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
