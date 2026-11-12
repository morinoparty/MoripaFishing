package party.morino.moripafishing.api.event.world

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * `WorldManager.createWorld` による釣りワールドの作成が成功した後に発生します。
 *
 * 作成した Bukkit ワールドの [FishingWorldLoadEvent] の後に発火するポストイベントであり、キャンセルはできません。
 *
 * @param worldId 作成された釣りワールドのID
 */
class FishingWorldCreateEvent(
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
