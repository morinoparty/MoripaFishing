package party.morino.moripafishing.api.event.fishing

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.FishDrawPool
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.world.LocationData

/**
 * このイベントは、釣りワールドで魚が掛かり、魚を抽選する直前に発生します。
 *
 * [getPool] の重みを変更すると、その釣り人のこの 1 回の抽選にだけ反映されます (撒き餌や餌による確率の変更など)。
 * 処理は次の順に進みます。
 *
 * 1. [AnglerFishDrawEvent] (このイベント。候補の重みを変更できる)
 * 2. 変更後の重みで抽選する
 * 3. [AnglerFishCaughtEvent] (釣果の差し替え・キャンセルができる)
 * 4. [AnglerFishCaughtResultEvent] (確定した釣果の通知)
 *
 * 候補が空でも発火します。全ての重みを 0 にすると抽選結果が無くなり、バニラのドロップがそのまま残ります。
 * メインスレッドで同期的に発火し、キャンセルはできません。
 *
 * @param angler 釣りをした釣り人
 * @param world 釣りをしている釣りワールド
 * @param hookLocation 釣り針の位置
 * @param pool 抽選の候補 (リスナーから重みを変更可能)
 */
class AnglerFishDrawEvent(
    private val angler: Angler,
    private val world: FishingWorld,
    private val hookLocation: LocationData,
    private val pool: FishDrawPool,
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

    /**
     * 釣りをした釣り人を取得します。
     * @return 釣り人
     */
    fun getAngler(): Angler = angler

    /**
     * 釣りをしている釣りワールドを取得します。
     * @return 釣りワールド
     */
    fun getWorld(): FishingWorld = world

    /**
     * 釣り針の位置を取得します。
     * @return 釣り針の位置
     */
    fun getHookLocation(): LocationData = hookLocation

    /**
     * 抽選の候補を取得します。重みの変更はこの抽選に反映されます。
     * @return 抽選の候補
     */
    fun getPool(): FishDrawPool = pool

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
