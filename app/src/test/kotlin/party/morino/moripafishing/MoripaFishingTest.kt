package party.morino.moripafishing

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.module
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.core.random.RandomizeManagerImpl
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.core.rarity.RarityManagerImpl
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.config.ConfigManagerImpl
import party.morino.moripafishing.config.PluginDirectoryMock
import party.morino.moripafishing.core.fish.FishManagerImpl
import party.morino.moripafishing.core.world.WorldManagerImpl

/**
 * MoripaFishingのテスト用拡張機能
 */
class MoripaFishingTest : BeforeAllCallback {
    /**
     * テスト開始前に呼び出されるメソッド
     * @param context 拡張機能のコンテキスト
     */
    override fun beforeAll(context: ExtensionContext) {
        val appModule = module {
            single<ConfigManager> { ConfigManagerImpl() }
            single<RandomizeManager> { RandomizeManagerImpl() }
            single<RarityManager> { RarityManagerImpl() }
            single<PluginDirectory> { PluginDirectoryMock() }
            single<FishManager> { FishManagerImpl() }
            single<WorldManager> { WorldManagerImpl() }
        }
        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }
    }
}