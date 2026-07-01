package party.morino.moripafishing.event.world

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * このイベントは、釣りワールドが削除される直前に発生します。
 *
 * 実際の削除処理の前に発火するプレイベントであり、キャンセルすると削除は行われません。
 *
 * @param worldId 削除対象の釣りワールドのID
 */
class FishingWorldDeleteEvent(
    private val worldId: FishingWorldId,
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

    private var isCancelled: Boolean = false

    override fun isCancelled(): Boolean = isCancelled

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }

    fun getWorldId(): FishingWorldId = worldId

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
