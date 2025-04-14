package party.morino.moripafishing

import org.incendo.cloud.parser.ParserParameters.single
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.core.context.startKoin
import org.koin.dsl.module
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.random.RandomizeManager
import party.morino.moripafishing.random.RandomizeManagerImpl
import party.morino.moripafishing.api.rarity.RarityManager
import party.morino.moripafishing.rarity.RarityManagerImpl
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.fish.FishManager
import party.morino.moripafishing.config.ConfigManagerImpl
import party.morino.moripafishing.config.PluginDirectoryMock
import party.morino.moripafishing.fish.FishManagerImpl

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
        }
        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }
    }
}