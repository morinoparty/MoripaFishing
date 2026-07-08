package party.morino.moripafishing.event.world

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * このイベントは、釣りワールドがアンロードされた後に発生します。
 *
 * ワールド削除時のアンロードと、プラグイン無効化時の解放の両方で発火します。
 * ポストイベントであり、キャンセルはできません。
 *
 * @param worldId アンロードされた釣りワールドのID
 */
class FishingWorldUnloadEvent(
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
