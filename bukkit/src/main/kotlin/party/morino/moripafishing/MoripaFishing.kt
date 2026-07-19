package party.morino.moripafishing

import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
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
import party.morino.moripafishing.api.core.world.weather.WeatherSource
import party.morino.moripafishing.config.ConfigManagerImpl
import party.morino.moripafishing.config.PluginDirectoryImpl
import party.morino.moripafishing.core.angler.AnglerManagerImpl
import party.morino.moripafishing.core.fish.FishManagerImpl
import party.morino.moripafishing.core.internationalization.TranslateManagerImpl
import party.morino.moripafishing.core.log.LogManagerImpl
import party.morino.moripafishing.core.random.RandomizeManagerImpl
import party.morino.moripafishing.core.rarity.RarityManagerImpl
import party.morino.moripafishing.core.world.FishingWorldImpl
import party.morino.moripafishing.core.world.WorldManagerImpl
import party.morino.moripafishing.core.world.weather.WeatherSourceCleanupListener
import party.morino.moripafishing.core.world.weather.WeatherSourceRegistry
import party.morino.moripafishing.core.world.weather.source.InternalWeatherSource
import party.morino.moripafishing.core.world.weather.source.VanillaWeatherSource
import party.morino.moripafishing.event.world.FishingWorldUnloadEvent
import party.morino.moripafishing.integrations.weather.api.WeatherControlProvider
import party.morino.moripafishing.integrations.worldlifecycle.api.WorldLifecycleProvider
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
    private val weatherSourceRegistry: WeatherSourceRegistry by inject()

    private var worldLifecycleProvider: WorldLifecycleProvider? = null

    private var weatherControlProvider: WeatherControlProvider? = null

    /**
     * プラグインの有効化時に呼び出されるメソッド
     */
    override fun onEnable() {
        setupKoin()
        // 外部プラグイン向けの安定した API 取得口を用意する
        MoripaFishingAPIProvider.register(this)
        server.servicesManager.register(MoripaFishingAPI::class.java, this, this, ServicePriority.Normal)
        resolveWorldLifecycleProvider()
        resolveWeatherControlProvider()
        registerBuiltInWeatherSources()
        initializeManagers()
        loadListeners()
        translateManager.load()
        logger.info("MoripaFishing enabled")
        updateWorlds()
    }

    /**
     * `MoripaFishing-Integration-WorldLifecycle` (softdepend) を検出し、`WorldLifecycleProvider` を実装していれば採用する。
     * 未導入時は `null` のままで、ワールド境界の同期やカスタムジェネレーターでのワールド作成機能は
     * スキップされる。
     */
    private fun resolveWorldLifecycleProvider() {
        val integrationName = "MoripaFishing-Integration-WorldLifecycle"
        val integrationPlugin = Bukkit.getPluginManager().getPlugin(integrationName)
        worldLifecycleProvider =
            when {
                integrationPlugin is WorldLifecycleProvider -> {
                    logger.info("WorldLifecycle integration detected: $integrationName")
                    integrationPlugin
                }
                integrationPlugin != null -> {
                    logger.warning(
                        "$integrationName is installed but does not implement WorldLifecycleProvider.",
                    )
                    null
                }
                else -> {
                    logger.info(
                        "WorldLifecycle integration not installed; border sync and custom generators are disabled.",
                    )
                    null
                }
            }
    }

    /**
     * `MoripaFishing-Integration-Weather` (softdepend) を検出し、`WeatherControlProvider` を実装していれば採用する。
     * 未導入時は `null` のままで、天候の適用 (ワールド改変) はスキップされる。
     * 天候の決定・参照はコアが行うため、未導入でも釣りの抽選条件には反映され続ける。
     */
    private fun resolveWeatherControlProvider() {
        val integrationName = "MoripaFishing-Integration-Weather"
        val integrationPlugin = Bukkit.getPluginManager().getPlugin(integrationName)
        weatherControlProvider =
            when {
                integrationPlugin is WeatherControlProvider -> {
                    logger.info("Weather integration detected: $integrationName")
                    integrationPlugin
                }
                integrationPlugin != null -> {
                    logger.warning(
                        "$integrationName is installed but does not implement WeatherControlProvider.",
                    )
                    null
                }
                else -> {
                    logger.info(
                        "Weather integration not installed; weather application to worlds is disabled.",
                    )
                    null
                }
            }
    }

    /**
     * 組み込みの天候ソース（`moripafishing:internal` / `moripafishing:vanilla`）をレジストリへ登録する。
     * 外部プラグインは `registerWeatherSource` で自前のソースを追加できる。
     */
    private fun registerBuiltInWeatherSources() {
        weatherSourceRegistry.register(InternalWeatherSource(_randomizeManager))
        weatherSourceRegistry.register(VanillaWeatherSource(this))
    }

    /**
     * マネージャーの初期化
     */
    private fun initializeManagers() {
        _worldManager.initializeWorlds()
    }

    override fun onDisable() {
        _worldManager.getWorlds().forEach { world ->
            // shutdown はソースを新たに解決しない (無効化中の Listener 登録例外を避ける)。
            // 天候リセットと外部プロバイダーの Listener 解放をまとめて行う。
            (world as? FishingWorldImpl)?.shutdown() ?: world.effectFinish()
            FishingWorldUnloadEvent(world.getId()).callEvent()
        }
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
                single<WorldManager> { WorldManagerImpl() }
                single<PluginDirectory> { PluginDirectoryImpl() }
                single<FishManager> { FishManagerImpl() }
                single<AnglerManager> { AnglerManagerImpl() }
                single<LogManager> { LogManagerImpl() }
                single<TranslateManager> { TranslateManagerImpl() }
                single<WeatherSourceRegistry> { WeatherSourceRegistry() }
            }

        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }
    }

    /**
     * ワールド状態の定期同期タスクを開始する。
     * プラグイン無効化時は Bukkit スケジューラーがタスクを自動的に破棄する。
     */
    private fun updateWorlds() {
        val intervalTicks = _configManager.getConfig().world.refreshInterval * 20L
        Bukkit.getScheduler().runTaskTimerAsynchronously(
            this,
            Runnable {
                _worldManager.getWorlds().forEach {
                    (it as? FishingWorldImpl)?.updateState()
                }
            },
            0L,
            intervalTicks,
        )
    }

    private fun loadListeners() {
        // コア機能: 釣りイベント処理は常に有効
        this.server.pluginManager.registerEvents(PlayerFishingListener(), this)
        // 無効化されたプラグインの WeatherSource を自動解除する
        this.server.pluginManager.registerEvents(WeatherSourceCleanupListener(), this)
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

    override fun registerWeatherSource(source: WeatherSource) {
        weatherSourceRegistry.register(source)
    }

    override fun unregisterWeatherSource(key: Key) {
        weatherSourceRegistry.unregister(key)
    }

    override fun getWeatherSource(key: Key): WeatherSource? = weatherSourceRegistry.get(key)

    override fun getWeatherSourceKeys(): Set<Key> = weatherSourceRegistry.getKeys()

    /**
     * WorldLifecycle Integration の `WorldLifecycleProvider` を返す。未導入時は `null`。
     *
     * `MoripaFishingAPI` には公開しない (`WorldLifecycleProvider` は独立モジュールにあり、
     * core は shade していないため)。外部プラグインは
     * `Bukkit.getPluginManager().getPlugin("MoripaFishing-Integration-WorldLifecycle") as? WorldLifecycleProvider`
     * で直接取得すること。
     */
    fun getWorldLifecycleProvider(): WorldLifecycleProvider? = worldLifecycleProvider

    /**
     * Weather Integration の `WeatherControlProvider` を返す。未導入時は `null`。
     *
     * `getWorldLifecycleProvider()` と同様に `MoripaFishingAPI` には公開しない
     * (`WeatherControlProvider` は独立モジュールにあり、core は shade していないため)。
     */
    fun getWeatherControlProvider(): WeatherControlProvider? = weatherControlProvider
}
