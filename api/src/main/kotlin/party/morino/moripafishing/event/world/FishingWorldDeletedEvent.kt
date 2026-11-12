package party.morino.moripafishing.event.world

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 釣りワールドの削除が完了した後に発生します。
 *
 * Bukkit ワールドがロード済みだった場合は、そのアンロード ([FishingWorldUnloadEvent]) の後に発火します。
 * 設定ファイルは削除済みで、ワールドフォルダは残ります。キャンセルはできません。
 * 削除を止めたい場合は [FishingWorldDeleteEvent] を使用してください。
 *
 * @param worldId 削除された釣りワールドのID
 */
class FishingWorldDeletedEvent(
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
