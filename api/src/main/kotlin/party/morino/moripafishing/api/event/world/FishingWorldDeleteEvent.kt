package party.morino.moripafishing.api.event.world

import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.event.CancellableMoripaFishingEvent
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * `WorldManager.deleteWorld` で釣りワールドが削除される直前に発生します。
 *
 * Bukkit ワールドのアンロードと設定ファイルの削除より前に発火するプレイベントであり、
 * キャンセルすると削除は行われません。削除完了後には [FishingWorldDeletedEvent] が発生します。
 *
 * @param worldId 削除対象の釣りワールドのID
 */
class FishingWorldDeleteEvent(
    private val worldId: FishingWorldId,
) : CancellableMoripaFishingEvent() {
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
