package party.morino.moripafishing.event.angler

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData

/**
 * このイベントは、プレイヤーの参加時に釣りワールドのスポーン地点へテレポートする直前に発生します。
 *
 * 参加時テレポートは `MoripaFishing-Integration-WorldLifecycle` によって実行されます。
 * このイベントをキャンセルすると、テレポートは行われずバニラ（あるいは他プラグイン）の
 * 参加動作に委ねられます。[destination] を差し替えることで、テレポート先を変更できます。
 *
 * @param player テレポート対象のプレイヤー
 * @param worldId テレポート先の釣りワールドのID
 * @param destination テレポート先の座標（リスナーから変更可能）
 */
class AnglerJoinTeleportEvent(
    private val player: Player,
    private val worldId: FishingWorldId,
    private var destination: LocationData,
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

    fun getPlayer(): Player = player

    fun getWorldId(): FishingWorldId = worldId

    fun getDestination(): LocationData = destination

    fun setDestination(destination: LocationData) {
        this.destination = destination
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
