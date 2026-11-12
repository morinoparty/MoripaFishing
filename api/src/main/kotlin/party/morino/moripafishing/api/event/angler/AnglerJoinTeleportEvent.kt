package party.morino.moripafishing.api.event.angler

import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.event.CancellableMoripaFishingEvent
import party.morino.moripafishing.api.model.world.LocationData

/**
 * このイベントは、プレイヤーの参加時に釣りワールドのスポーン地点へテレポートする直前に発生します。
 *
 * 参加時テレポートは `MoripaFishing-Integration-WorldLifecycle` によって実行されます。
 * このイベントをキャンセルすると、テレポートは行われずバニラ（あるいは他プラグイン）の
 * 参加動作に委ねられます。[setDestination] でテレポート先を変更できます。
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

    /**
     * テレポート対象の釣り人を取得します。
     * @return 釣り人
     */
    fun getAngler(): Angler = angler

    /**
     * テレポート先を取得します。
     * @return テレポート先の座標
     */
    fun getDestination(): LocationData = destination

    /**
     * テレポート先を変更します。
     * @param destination 新しいテレポート先の座標
     */
    fun setDestination(destination: LocationData) {
        this.destination = destination
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
