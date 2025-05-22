package party.morino.moripafishing.api.core.fishing

    import party.morino.moripafishing.api.core.fishing.lure.WaitTimeManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 釣りの管理を行うインターフェース
 * 釣れるまでの時間やレアリティの変更を管理する
 */
interface FishingManager {

    /**
     * WaitTimeManagerを取得する
     * @return WaitTimeManager
     */
    fun getWaitTimeManager(): WaitTimeManager

}
