package party.morino.moripafishing.core.fishing

import org.koin.core.component.KoinComponent
import party.morino.moripafishing.api.core.fishing.FishingManager
import party.morino.moripafishing.api.core.fishing.WaitTimeManager

/**
 * FishingManagerの実装クラス
 */
class FishingManagerImpl : FishingManager, KoinComponent {
    // WaitTimeManagerのインスタンスを保持
    private val waitTimeManager: WaitTimeManager = WaitTimeManagerImpl()

    /**
     * WaitTimeManagerを返す
     */
    override fun getWaitTimeManager(): WaitTimeManager = waitTimeManager
}

