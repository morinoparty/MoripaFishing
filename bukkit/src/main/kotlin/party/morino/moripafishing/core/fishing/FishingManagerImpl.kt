package party.morino.moripafishing.core.fishing

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.fishing.FishingManager
import party.morino.moripafishing.api.core.fishing.WaitTimeManager
import party.morino.moripafishing.api.core.rod.RodPresetManager

/**
 * FishingManagerの実装クラス
 */
class FishingManagerImpl : FishingManager, KoinComponent {
    // WaitTimeManagerのインスタンスを保持
    private val waitTimeManager: WaitTimeManager = WaitTimeManagerImpl()

    // RodPresetManagerをKoinから注入
    private val _rodPresetManager: RodPresetManager by inject()

    /**
     * WaitTimeManagerを返す
     */
    override fun getWaitTimeManager(): WaitTimeManager = waitTimeManager

    /**
     * RodPresetManagerを返す
     */
    override fun getRodPresetManager(): RodPresetManager = _rodPresetManager
}
