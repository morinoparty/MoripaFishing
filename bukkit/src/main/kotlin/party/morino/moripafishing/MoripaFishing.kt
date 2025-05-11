package party.morino.moripafishing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
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
import party.morino.moripafishing.core.internationalization.TranslateManager
import party.morino.moripafishing.core.random.RandomizeManagerImpl
import party.morino.moripafishing.core.rarity.RarityManagerImpl
import party.morino.moripafishing.core.world.WorldManagerImpl
import party.morino.moripafishing.listener.minecraft.PlayerFishingListener
import party.morino.moripafishing.listener.minecraft.PlayerJoinListener
import party.morino.moripafishing.utils.coroutines.async

class MoripaFishing : JavaPlugin(), MoripaFishingAPI {
    private lateinit var configManager: ConfigManager

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
        // databaseManager.initialize()
        worldManager.initializeWorlds()

        updateWorlds()
        // リスナーの登録
        loadListeners()

        // i18n
        TranslateManager.load()
        logger.info("MoripaFishing enabled")
    }

    private fun getInstanceForAPI() {
        configManager = GlobalContext.get().get()
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
        val appModule =
            module {
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

    private fun updateWorlds() {
        Bukkit.getScheduler().runTaskAsynchronously(
            this,
            Runnable {
                runBlocking {
                    withContext(Dispatchers.async) {
                        val interval = configManager.getConfig().world.refreshInterval * 1000L
                        while (true) {
                            worldManager.getWorldIdList().forEach {
                                worldManager.getWorld(it).updateState()
                            }
                            delay(interval)
                        }
                    }
                }
            },
        )
    }

    private fun loadListeners() {
        this.server.pluginManager.registerEvents(PlayerFishingListener(), this)
        this.server.pluginManager.registerEvents(
            PlayerJoinListener(),
            this,
        )
    }

    override fun getConfigManager(): ConfigManager {
        return configManager
    }

    override fun getRandomizeManager(): RandomizeManager {
        return randomizeManager
    }

    override fun getFishManager(): FishManager {
        return fishManager
    }

    override fun getWorldManager(): WorldManager {
        return worldManager
    }

    override fun getPluginDirectory(): PluginDirectory {
        return pluginDirectory
    }

    override fun getAnglerManager(): AnglerManager {
        return anglerManager
    }
}
