package party.morino.moripafishing.api.core.fishing

import party.morino.moripafishing.api.core.fishing.rod.RodPresetManager

/**
 * 釣りの管理を行うインターフェース
 * 釣れるまでの時間やレアリティの変更、ロッドプリセットを管理する
 */
interface FishingManager {
    /**
     * WaitTimeManagerを取得する
     * @return WaitTimeManager
     */
    fun getWaitTimeManager(): WaitTimeManager

    /**
     * RodPresetManagerを取得する
     * @return RodPresetManager
     */
    fun getRodPresetManager(): RodPresetManager
}
