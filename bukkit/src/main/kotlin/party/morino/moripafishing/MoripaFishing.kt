package party.morino.moripafishing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kyori.adventure.key.Key
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
import party.morino.moripafishing.api.core.internationalization.TranslateManager
import party.morino.moripafishing.api.core.log.LogManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.core.world.lifecycle.WorldLifecycleProvider
import party.morino.moripafishing.api.core.world.weather.WeatherSource
import party.morino.moripafishing.api.core.world.weather.control.WeatherControlProvider
import party.morino.moripafishing.config.ConfigManagerImpl
import party.morino.moripafishing.config.PluginDirectoryImpl
import party.morino.moripafishing.core.angler.AnglerManagerImpl
import party.morino.moripafishing.core.fish.FishManagerImpl
import party.morino.moripafishing.core.internationalization.TranslateManagerImpl
import party.morino.moripafishing.core.log.LogManagerImpl
import party.morino.moripafishing.core.random.RandomizeManagerImpl
import party.morino.moripafishing.core.rarity.RarityManagerImpl
import party.morino.moripafishing.core.world.WorldManagerImpl
import party.morino.moripafishing.core.world.weather.WeatherSourceRegistry
import party.morino.moripafishing.core.world.weather.source.InternalWeatherSource
import party.morino.moripafishing.core.world.weather.source.VanillaWeatherSource
import party.morino.moripafishing.listener.minecraft.PlayerFishingListener
import party.morino.moripafishing.utils.coroutines.async

open class MoripaFishing :
    JavaPlugin(),
    MoripaFishingAPI {
    // 各マネージャーのインスタンスをKoinから遅延初期化
    private val _configManager: ConfigManager by lazy { GlobalContext.get().get() }
    private val _randomizeManager: RandomizeManager by lazy { GlobalContext.get().get() }
    private val _rarityManager: RarityManager by lazy { GlobalContext.get().get() }
    private val _pluginDirectory: PluginDirectory by lazy { GlobalContext.get().get() }
    private val _worldManager: WorldManager by lazy { GlobalContext.get().get() }
    private val _fishManager: FishManager by lazy { GlobalContext.get().get() }
    private val _anglerManager: AnglerManager by lazy { GlobalContext.get().get() }
    private val _logManager: LogManager by lazy { GlobalContext.get().get() }
    private val _translateManager: TranslateManager by lazy { GlobalContext.get().get() }
    private val _weatherSourceRegistry: WeatherSourceRegistry by lazy { GlobalContext.get().get() }

    private var disable = false

    private var worldLifecycleProvider: WorldLifecycleProvider? = null

    private var weatherControlProvider: WeatherControlProvider? = null

    /**
     * プラグインの有効化時に呼び出されるメソッド
     */
    override fun onEnable() {
        setupKoin()
        resolveWorldLifecycleProvider()
        resolveWeatherControlProvider()
        registerBuiltInWeatherSources()
        initializeManagers()
        loadListeners()
        _translateManager.load()
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
        _weatherSourceRegistry.register(InternalWeatherSource(_randomizeManager))
        _weatherSourceRegistry.register(VanillaWeatherSource(this))
    }

    /**
     * マネージャーの初期化
     */
    private fun initializeManagers() {
        _worldManager.initializeWorlds()
    }

    override fun onDisable() {
        disable = true
        _worldManager.getWorldIdList().forEach {
            _worldManager.getWorld(it).effectFinish()
        }
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

    private fun updateWorlds() {
        Bukkit.getScheduler().runTaskAsynchronously(
            this,
            Runnable {
                runBlocking {
                    withContext(Dispatchers.async) {
                        val interval = _configManager.getConfig().world.refreshInterval * 1000L
                        // whileループにラベルを付けて、ラムダ内からreturn@runWhileで抜ける
                        runWhile@ while (!disable) {
                            _worldManager.getWorldIdList().forEach {
                                _worldManager.getWorld(it).updateState()
                            }
                            repeat(10) {
                                if (disable) return@repeat // whileループごと抜ける
                                delay(interval / 10)
                            }
                        }
                    }
                }
            },
        )
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

    override fun registerWeatherSource(source: WeatherSource) {
        _weatherSourceRegistry.register(source)
    }

    override fun unregisterWeatherSource(key: Key) {
        _weatherSourceRegistry.unregister(key)
    }

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
