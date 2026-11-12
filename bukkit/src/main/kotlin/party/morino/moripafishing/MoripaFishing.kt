package party.morino.moripafishing

import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.bind
import org.koin.dsl.module
import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.MoripaFishingAPIProvider
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.internationalization.TranslateManager
import party.morino.moripafishing.api.core.log.LogManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.config.ConfigManagerImpl
import party.morino.moripafishing.config.PluginDirectoryImpl
import party.morino.moripafishing.core.angler.AnglerManagerImpl
import party.morino.moripafishing.core.fish.FishManagerImpl
import party.morino.moripafishing.core.internationalization.TranslateManagerImpl
import party.morino.moripafishing.core.log.LogManagerImpl
import party.morino.moripafishing.core.random.RandomizeManagerImpl
import party.morino.moripafishing.core.rarity.RarityManagerImpl
import party.morino.moripafishing.core.world.WorldConfigStore
import party.morino.moripafishing.core.world.WorldManagerImpl
import party.morino.moripafishing.listener.minecraft.PlayerFishingListener

open class MoripaFishing :
    JavaPlugin(),
    MoripaFishingAPI,
    KoinComponent {
    // 各マネージャーのインスタンスをKoinから遅延初期化 (setupKoin 後に解決される)
    private val _configManager: ConfigManager by inject()
    private val _randomizeManager: RandomizeManager by inject()
    private val _rarityManager: RarityManager by inject()
    private val _pluginDirectory: PluginDirectory by inject()
    private val _worldManager: WorldManager by inject()
    private val _fishManager: FishManager by inject()
    private val _anglerManager: AnglerManager by inject()
    private val _logManager: LogManager by inject()
    private val translateManager: TranslateManager by inject()

    /**
     * プラグインの有効化時に呼び出されるメソッド
     */
    override fun onEnable() {
        setupKoin()
        // 外部プラグイン向けの安定した API 取得口を用意する
        MoripaFishingAPIProvider.register(this)
        server.servicesManager.register(MoripaFishingAPI::class.java, this, this, ServicePriority.Normal)
        // 翻訳の読み込みが定義済みワールドを参照するため、先に定義を読み込む
        worldRuntime()?.enable()
        loadListeners()
        translateManager.load()
        logger.info("MoripaFishing enabled")
    }

    override fun onDisable() {
        worldRuntime()?.disable()
        MoripaFishingAPIProvider.unregister()
        server.servicesManager.unregisterAll(this)
        logger.info("MoripaFishing disabled")
    }

    private fun setupKoin() {
        // テスト環境では既にKoinが初期化されている場合があるのでチェック
        if (getOrNull() != null) {
            return
        }

        val appModule =
            module {
                single<MoripaFishing> { this@MoripaFishing }
                single<ConfigManager> { ConfigManagerImpl() }
                single<RandomizeManager> { RandomizeManagerImpl() }
                single<RarityManager> { RarityManagerImpl() }
                single {
                    WorldManagerImpl(
                        this@MoripaFishing,
                        get(),
                        WorldConfigStore(get<PluginDirectory>().getWorldDirectory(), this@MoripaFishing.logger),
                    )
                } bind WorldManager::class
                single<PluginDirectory> { PluginDirectoryImpl() }
                single<FishManager> { FishManagerImpl() }
                single<AnglerManager> { AnglerManagerImpl() }
                single<LogManager> { LogManagerImpl() }
                single<TranslateManager> { TranslateManagerImpl() }
            }

        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }
    }

    private fun loadListeners() {
        // コア機能: 釣りイベント処理は常に有効
        this.server.pluginManager.registerEvents(PlayerFishingListener(), this)
    }

    // API getters - 式本体で簡潔に
    override fun getConfigManager(): ConfigManager = _configManager

    override fun getRandomizeManager(): RandomizeManager = _randomizeManager

    override fun getFishManager(): FishManager = _fishManager

    override fun getRarityManager(): RarityManager = _rarityManager

    override fun getWorldManager(): WorldManager = _worldManager

    override fun getPluginDirectory(): PluginDirectory = _pluginDirectory

    override fun getAnglerManager(): AnglerManager = _anglerManager

    override fun getLogManager(): LogManager = _logManager

    /**
     * ワールド管理のランタイム。`WorldManager` の束縛方法によらず実体から引くため、
     * テストハーネスで `WorldManager` にモックが束縛されている場合だけ `null` になる。
     */
    private fun worldRuntime(): WorldManagerImpl? = _worldManager as? WorldManagerImpl
}
