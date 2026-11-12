package party.morino.moripafishing.event.world

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 定義済みの釣りワールドの Bukkit ワールドがアンロードされた時に発生します。
 *
 * Bukkit の `WorldUnloadEvent` に追従して発火します (ワールド削除時のアンロードを含む)。
 * プラグインの無効化時には発火しません。キャンセルはできません。
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
