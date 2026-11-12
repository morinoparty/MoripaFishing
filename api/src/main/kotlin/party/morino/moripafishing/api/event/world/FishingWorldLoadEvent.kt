package party.morino.moripafishing.api.event.world

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 定義済みの釣りワールドの Bukkit ワールドがロードされ、利用可能になった後に発生します。
 *
 * Bukkit の `WorldLoadEvent` に追従して発火します。サーバー起動時 (`ServerLoadEvent`) に既にロード済みのワールドと、
 * `WorldManager.reload` で新たに定義されたワールドのうち既にロード済みのものについても発火します。
 * コアがワールドの状態を Integration に適用させた後に発火するポストイベントであり、キャンセルはできません。
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
