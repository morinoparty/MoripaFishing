package party.morino.moripafishing.event.angler

import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.event.CancellableMoripaFishingEvent

/**
 * このイベントは、プレイヤーの参加時に釣りワールドのスポーン地点へテレポートする直前に発生します。
 *
 * 参加時テレポートは `MoripaFishing-Integration-WorldLifecycle` によって実行されます。
 * このイベントをキャンセルすると、テレポートは行われずバニラ（あるいは他プラグイン）の
 * 参加動作に委ねられます。[setDestination] でテレポート先を変更できます。
 * テレポート先のワールドは `destination.worldId` で参照できます。
 *
 * @param angler テレポート対象の釣り人
 * @param destination テレポート先の座標（リスナーから変更可能）
 */
class AnglerJoinTeleportEvent(
    private val angler: Angler,
    private var destination: LocationData,
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

    fun getAngler(): Angler = angler

    fun getDestination(): LocationData = destination

    fun setDestination(destination: LocationData) {
        this.destination = destination
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
