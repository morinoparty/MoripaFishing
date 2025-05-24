package party.morino.moripafishing.api.core.fishing

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
