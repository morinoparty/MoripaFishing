package party.morino.moripafishing.core.fishing

import org.incendo.cloud.parser.ParserParameters.single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import party.morino.moripafishing.api.core.fishing.FishingManager
import party.morino.moripafishing.api.core.fishing.WaitTimeManager
import party.morino.moripafishing.api.core.fishing.rod.RodPresetManager
import party.morino.moripafishing.core.fishing.rod.RodPresetManagerImpl

/**
 * FishingManagerの実装クラス
 */
class FishingManagerImpl : FishingManager, KoinComponent {
    init {
        val fishingManagerModule =
            module {
                single<WaitTimeManager> { WaitTimeManagerImpl() }
                single<RodPresetManager> { RodPresetManagerImpl() }
            }
        loadKoinModules(fishingManagerModule)
    }

    // WaitTimeManagerのインスタンスを保持
    private val _waitTimeManager: WaitTimeManager by inject()

    // RodPresetManagerをKoinから注入
    private val _rodPresetManager: RodPresetManager by inject()

    /**
     * WaitTimeManagerを返す
     */
    override fun getWaitTimeManager(): WaitTimeManager = _waitTimeManager

    /**
     * RodPresetManagerを返す
     */
    override fun getRodPresetManager(): RodPresetManager = _rodPresetManager
}
