package party.morino.moripafishing

import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.startKoin
import org.koin.dsl.module
import party.morino.moripafishing.api.random.RandomizeManager
import party.morino.moripafishing.random.RandomizeManagerImpl
import party.morino.moripafishing.api.rarity.RarityManager
import party.morino.moripafishing.rarity.RarityManagerImpl
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.config.PluginDirectoryImpl
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.fish.FishManager
import party.morino.moripafishing.config.ConfigManagerImpl
import party.morino.moripafishing.fish.FishManagerImpl

class MoripaFishing : JavaPlugin() {
    // ランダム化マネージャーのインスタンスを遅延初期化する
    private lateinit var randomizeManager: RandomizeManager
    // レアリティマネージャーのインスタンスを遅延初期化する
    private lateinit var rarityManager: RarityManager
    // プラグインディレクトリのインスタンスを遅延初期化する
    private lateinit var pluginDirectory: PluginDirectory

    /**
     * プラグインの有効化時に呼び出されるメソッド
     *
     * このメソッドでは以下の初期化処理を行います：
     * 1. Koinの初期化と依存性の設定
     * 2. 各マネージャーのインスタンス化
     * 3. データベースの初期化
     */
    override fun onEnable() {
        // Koinの設定
        setupKoin()
        // RandomizeManagerのインスタンスを取得
        randomizeManager = GlobalContext.get().get()
        // RarityManagerのインスタンスを取得
        rarityManager = GlobalContext.get().get()
        // PluginDirectoryのインスタンスを取得
        pluginDirectory = GlobalContext.get().get()

        // データベースの初期化
        // databaseManager.initialize()

        logger.info("MoripaFishing enabled")
    }

    override fun onDisable() {
        // プラグインが無効化された際のログ出力
        logger.info("MoripaFishing disabled")
    }

    private fun setupKoin() {
        val appModule = module {
            single<ConfigManager> { ConfigManagerImpl() }
            single<RandomizeManager> { RandomizeManagerImpl() }
            single<RarityManager> { RarityManagerImpl() }
            single<PluginDirectory> { PluginDirectoryImpl() }
            single<FishManager> { FishManagerImpl() }
        }
        
    
        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }
    }
}