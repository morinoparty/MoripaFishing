package party.morino.moripafishing

import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.module
import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.config.ConfigManagerImpl
import party.morino.moripafishing.config.PluginDirectoryImpl
import party.morino.moripafishing.core.angler.AnglerManagerImpl
import party.morino.moripafishing.core.fish.FishManagerImpl
import party.morino.moripafishing.core.random.RandomizeManagerImpl
import party.morino.moripafishing.core.rarity.RarityManagerImpl
import party.morino.moripafishing.core.world.WorldManagerImpl

class MoripaFishing : JavaPlugin(), MoripaFishingAPI {
    // ランダム化マネージャーのインスタンスを遅延初期化する
    private lateinit var randomizeManager: RandomizeManager
    // レアリティマネージャーのインスタンスを遅延初期化する
    private lateinit var rarityManager: RarityManager
    // プラグインディレクトリのインスタンスを遅延初期化する
    private lateinit var pluginDirectory: PluginDirectory
    // WorldManagerのインスタンスを遅延初期化する
    private lateinit var worldManager: WorldManager
    // FishManagerのインスタンスを遅延初期化する
    private lateinit var fishManager: FishManager
    // AnglerManagerのインスタンスを遅延初期化する
    private lateinit var anglerManager: AnglerManager

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
        getInstanceForAPI()

        // データベースの初期化
//         databaseManager.initialize()

        logger.info("MoripaFishing enabled")
    }

    private fun getInstanceForAPI() {
        // PluginDirectoryのインスタンスを取得
        pluginDirectory = GlobalContext.get().get()
        // RarityManagerのインスタンスを取得
        rarityManager = GlobalContext.get().get()
        // RandomizeManagerのインスタンスを取得
        randomizeManager = GlobalContext.get().get()
        // WorldManagerのインスタンスを取得
        worldManager = GlobalContext.get().get()
        // FishManagerのインスタンスを取得
        fishManager = GlobalContext.get().get()
        // AnglerManagerのインスタンスを取得
        anglerManager = GlobalContext.get().get()
    }

    override fun onDisable() {
        // プラグインが無効化された際のログ出力
        logger.info("MoripaFishing disabled")
    }

    private fun setupKoin() {
        val appModule = module {
            single<MoripaFishing> { this@MoripaFishing }
            single<ConfigManager> { ConfigManagerImpl() }
            single<RandomizeManager> { RandomizeManagerImpl() }
            single<RarityManager> { RarityManagerImpl() }
            single<WorldManager> { WorldManagerImpl() }
            single<PluginDirectory> { PluginDirectoryImpl() }
            single<FishManager> { FishManagerImpl() }
            single<AnglerManager> { AnglerManagerImpl() }
        }

        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }
    }

    override fun getConfigManager(): ConfigManager {
        return GlobalContext.get().get()
    }

    override fun getRandomizeManager(): RandomizeManager {
        return GlobalContext.get().get()
    }

    override fun getFishManager(): FishManager {
        return GlobalContext.get().get()
    }

    override fun getWorldManager(): WorldManager {
        return GlobalContext.get().get()
    }

    override fun getPluginDirectory(): PluginDirectory {
        return GlobalContext.get().get()
    }

    override fun getAnglerManager(): AnglerManager {
        return GlobalContext.get().get()
    }
}