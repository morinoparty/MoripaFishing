package party.morino.moripafishing

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.module
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.fishing.FishingManager
import party.morino.moripafishing.api.core.fishing.rod.RodPresetManager
import party.morino.moripafishing.api.core.log.LogManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.fish.FishProbabilityManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.config.ConfigManagerImpl
import party.morino.moripafishing.core.fish.FishManagerImpl
import party.morino.moripafishing.core.fishing.FishingManagerImpl
import party.morino.moripafishing.core.fishing.rod.RodPresetManagerImpl
import party.morino.moripafishing.core.random.RandomizeManagerImpl
import party.morino.moripafishing.core.random.fish.FishProbabilityManagerImpl
import party.morino.moripafishing.core.rarity.RarityManagerImpl
import party.morino.moripafishing.mocks.config.PluginDirectoryMock
import party.morino.moripafishing.mocks.log.LogManagerMock
import party.morino.moripafishing.mocks.world.WorldManagerMock

/**
 * MoripaFishingのテスト用拡張機能
 * KoinとMockBukkitのセットアップを統合する
 */
class MoripaFishingTest : BeforeAllCallback, AfterAllCallback {
    companion object {
        @JvmStatic
        var server: ServerMock? = null
            private set

        @JvmStatic
        fun getMockServer(): ServerMock {
            return server ?: throw IllegalStateException("MockBukkit server is not initialized")
        }
    }

    /**
     * テスト開始前に呼び出されるメソッド
     * KoinとMockBukkitの初期化を行う
     * @param context 拡張機能のコンテキスト
     */
    override fun beforeAll(context: ExtensionContext) {
        // MockBukkitの初期化
        if (server == null) {
            server = MockBukkit.mock()
        }
        // Koinの初期化
        val appModule =
            module {
                single<ConfigManager> { ConfigManagerImpl() }
                single<RandomizeManager> { RandomizeManagerImpl() }
                single<RarityManager> { RarityManagerImpl() }
                single<PluginDirectory> { PluginDirectoryMock() }
                single<FishManager> { FishManagerImpl() }
                single<WorldManager> { WorldManagerMock() }
                single<LogManager> { LogManagerMock() }
                single<RodPresetManager> { RodPresetManagerImpl() }
                single<FishingManager> { FishingManagerImpl() }
                single<FishProbabilityManager> { FishProbabilityManagerImpl() }
                single<ServerMock> { server!! } // MockBukkitサーバーをKoinに登録
            }
        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }
    }

    /**
     * テスト終了後に呼び出されるメソッド
     * MockBukkitのクリーンアップを行う
     * @param context 拡張機能のコンテキスト
     */
    override fun afterAll(context: ExtensionContext) {
        // MockBukkitのクリーンアップ
        server?.let {
            MockBukkit.unmock()
            server = null
        }
    }
}
