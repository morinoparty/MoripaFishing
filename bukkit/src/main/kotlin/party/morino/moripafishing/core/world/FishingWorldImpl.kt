package party.morino.moripafishing.core.world

import kotlinx.serialization.json.decodeFromStream
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.config.world.WorldDetailConfig
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.core.world.weather.WeatherSource
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.world.weather.WeatherSourceRegistry
import party.morino.moripafishing.event.world.FishingWorldWeatherChangeEvent
import party.morino.moripafishing.utils.IntegrationCall
import party.morino.moripafishing.utils.Utils
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

/**
 * [FishingWorld] のコア実装。
 *
 * コアは天候・時刻・ゲームルール値などを「決定」するだけで、Bukkit ワールドへの書き込みは一切行わない。
 * 決定した値の適用は Integration (`WeatherControlProvider` / `WorldLifecycleProvider`) に委ね、
 * 未導入の場合は適用をスキップする。これにより、コアのみを導入したサーバーでは
 * ワールドの天候・時刻・ゲームルール・スポーン・ボーダーが変更されない。
 */
@kotlinx.serialization.ExperimentalSerializationApi
class FishingWorldImpl(
    private val worldId: FishingWorldId,
) : FishingWorld,
    KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val pluginDirectory: PluginDirectory by inject()
    private val configManager: ConfigManager by inject()
    private val randomizeManager: RandomizeManager by inject()
    private val weatherSourceRegistry: WeatherSourceRegistry by inject()

    private lateinit var worldDetailConfig: WorldDetailConfig

    private val weatherRandomizer: WeatherRandomizer by lazy {
        randomizeManager.getWeatherRandomizer(worldId)
    }

    @Volatile
    private var weatherProvider: WeatherProvider? = null

    @Volatile
    private var resolvedSource: WeatherSource? = null
    private val weatherProviderLock = Any()

    /**
     * 最後に把握した実効天候。適用の重複抑制と `FishingWorldWeatherChangeEvent` の発火判定に使う。
     */
    @Volatile
    private var lastKnownWeather: WeatherType? = null

    /**
     * `setWeather` による手動上書き。プロバイダーの決定が上書き時点から変わるまで有効。
     */
    @Volatile
    private var manualWeather: WeatherType? = null

    /**
     * 手動上書きを設定した時点でのプロバイダーの決定。決定が変わったら上書きを破棄する判定に使う。
     */
    @Volatile
    private var manualWeatherBaseline: WeatherType? = null

    /**
     * `applyWeatherEffects = false` へ切り替えた際に、Weather Integration 経由で `DO_WEATHER_CYCLE` を
     * バニラ既定値へ戻したかどうか (ワールドデータに強制値が残るのを防ぐ)。
     */
    @Volatile
    private var weatherCycleReleased = false

    /**
     * 未登録のためフォールバックしたキーの警告を1度だけ出すための記録。
     */
    private val warnedMissingSource: MutableSet<Key> = ConcurrentHashMap.newKeySet()

    /**
     * `ClimateConfig.weatherSource` のキーから `WeatherSource` を解決する。
     *
     * プロバイダーはワールドごとに1つだけ生成・保持する必要があるため
     * (例: `VanillaWeatherProvider` は Bukkit Listener を登録するので二重登録を避ける)、
     * double-checked locking で解決を直列化する。
     *
     * 設定されたキーが未登録の場合は `moripafishing:internal` にフォールバックしつつ、
     * 後から外部ソースが登録された際に解決し直せるよう、フォールバック中はキャッシュを
     * 設定キーに固定しない。
     */
    private fun resolveWeatherSource(): WeatherSource {
        val configKey = currentClimateConfig().weatherSource
        val cached = resolvedSource
        if (cached != null && cached.key == configKey) return cached
        return synchronized(weatherProviderLock) {
            val current = resolvedSource
            if (current != null && current.key == configKey) return@synchronized current

            val source = weatherSourceRegistry.get(configKey)
            if (source != null) {
                warnedMissingSource.remove(configKey)
                swapProvider(source)
                return@synchronized source
            }

            if (warnedMissingSource.add(configKey)) {
                plugin.logger.warning(
                    "[${worldId.value}] weatherSource '$configKey' is not registered; " +
                        "falling back to '${WeatherSource.INTERNAL}'. " +
                        "Register it via MoripaFishingAPI.registerWeatherSource.",
                )
            }
            val fallback =
                current?.takeIf { it.key == WeatherSource.INTERNAL }
                    ?: weatherSourceRegistry.get(WeatherSource.INTERNAL)
                    ?: error("Built-in weather source '${WeatherSource.INTERNAL}' is not registered")
            if (current !== fallback) {
                swapProvider(fallback)
            }
            fallback
        }
    }

    /**
     * 解決済みソースとプロバイダーを差し替える。呼び出し側で [weatherProviderLock] を保持していること。
     */
    private fun swapProvider(source: WeatherSource) {
        weatherProvider?.dispose()
        resolvedSource = source
        weatherProvider = source.createProvider(worldId)
        manualWeather = null
        manualWeatherBaseline = null
    }

    /**
     * 指定キーのソースを使用中であれば、解決キャッシュを破棄しプロバイダーを dispose する。
     * `WeatherSourceRegistry` の解除・上書き時に呼ばれ、次回アクセスで再解決させる。
     */
    fun invalidateWeatherSource(key: Key) {
        synchronized(weatherProviderLock) {
            if (resolvedSource?.key != key) return
            weatherProvider?.dispose()
            weatherProvider = null
            resolvedSource = null
        }
    }

    /**
     * プロバイダーを破棄する。ワールド削除時・プラグイン無効化時に呼ばれる。
     */
    fun disposeWeatherProvider() {
        synchronized(weatherProviderLock) {
            weatherProvider?.dispose()
            weatherProvider = null
            resolvedSource = null
        }
    }

    /**
     * プラグイン無効化時の後始末。ソースを新たに解決しない
     * (無効化中の Listener 登録で例外になるため、既に解決済みの場合のみ天候をリセットする)。
     */
    fun shutdown() {
        synchronized(weatherProviderLock) {
            val source = resolvedSource
            if (source != null && source.managesWorldWeather && currentClimateConfig().applyWeatherEffects) {
                plugin.getWeatherControlProvider()?.resetWeather(worldKey)
            }
            weatherProvider?.dispose()
            weatherProvider = null
            resolvedSource = null
        }
    }

    private fun weatherProvider(): WeatherProvider {
        resolveWeatherSource()
        return weatherProvider ?: error("Weather provider is not initialized for ${worldId.value}")
    }

    init {
        loadConfig()
        plugin.logger.info("FishingWorldImpl(${worldId.value}) initialized")
    }

    /**
     * ワールドの詳細設定を再読み込みする。コア内部 (リロード・初期化) 用。
     */
    fun loadConfig() {
        val file = pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        if (!file.exists()) {
            throw IllegalArgumentException("World detail config file not found: ${file.absolutePath}")
        }
        worldDetailConfig = Utils.json.decodeFromStream<WorldDetailConfig>(file.inputStream())
        // ソースが変わり得るのでプロバイダーキャッシュを無効化する。
        // Listener 等を登録しているプロバイダーは dispose で解放させる。
        synchronized(weatherProviderLock) {
            weatherProvider?.dispose()
            weatherProvider = null
            resolvedSource = null
            warnedMissingSource.clear()
            manualWeather = null
            manualWeatherBaseline = null
            weatherCycleReleased = false
        }
    }

    private val worldKey: String by lazy { worldId.toWorldKey().asString() }

    override fun getWorldDetails(): WorldDetailConfig = worldDetailConfig

    override fun getId(): FishingWorldId = worldId

    override fun getCalculatedWeather(): WeatherType = weatherRandomizer.drawWeather()

    override fun getCurrentWeather(): WeatherType = manualWeather ?: weatherProvider().getCurrentWeather(worldId)

    /**
     * 天候を設定する。
     *
     * ソースがワールド天候を駆動する場合 (`managesWorldWeather == true`) は、プロバイダーの決定が
     * 次に変わるまで有効な手動上書きとして扱い、`getCurrentWeather` (抽選条件) にも反映する。
     * `ClimateConfig.applyWeatherEffects` が有効で Weather Integration が導入されていれば Bukkit ワールドにも反映する。
     * それ以外のソースでは MoripaFishing がワールドを支配しないため no-op。
     * 実効天候が変化した場合は `FishingWorldWeatherChangeEvent` を発火する。
     */
    override fun setWeather(weatherType: WeatherType) {
        val source = resolveWeatherSource()
        if (!source.managesWorldWeather) {
            plugin.logger.fine(
                "[${worldId.value}] setWeather($weatherType) ignored: weatherSource '${source.key}' does not manage world weather.",
            )
            return
        }
        manualWeather = weatherType
        manualWeatherBaseline = weatherProvider().getCurrentWeather(worldId)
        applyEffectiveWeather(weatherType)
    }

    override fun updateWeather() {
        if (resolveWeatherSource().managesWorldWeather) {
            refreshManagedWeather()
        } else {
            observeWeather()
        }
    }

    /**
     * 管理ソースの天候をリフレッシュする。手動上書きはプロバイダーの決定が
     * 上書き時点から変わっていない間だけ維持し、変わったら破棄して決定に従う。
     */
    private fun refreshManagedWeather() {
        val decision = weatherProvider().getCurrentWeather(worldId)
        val manual = manualWeather
        val effective =
            if (manual != null && decision == manualWeatherBaseline) {
                manual
            } else {
                manualWeather = null
                manualWeatherBaseline = null
                decision
            }
        applyEffectiveWeather(effective)
    }

    /**
     * 実効天候を適用する。値が変わらなければ Bukkit への再適用とイベント発火を省く (プロバイダー非依存)。
     */
    private fun applyEffectiveWeather(weatherType: WeatherType) {
        val previous = lastKnownWeather
        if (previous == weatherType) return
        lastKnownWeather = weatherType
        if (currentClimateConfig().applyWeatherEffects) {
            plugin.getWeatherControlProvider()?.applyWeather(
                worldKey = worldKey,
                weatherType = weatherType.name,
            ) ?: plugin.logger.fine(
                "[${worldId.value}] applyWeather($weatherType) skipped: WeatherControlProvider is not available.",
            )
        }
        fireWeatherChangeEvent(previous, weatherType)
    }

    /**
     * ワールド天候を駆動しないソース (vanilla / 外部 read-only) の実効天候変化を検知して
     * `FishingWorldWeatherChangeEvent` を発火する。リフレッシュループから呼ばれる。
     */
    private fun observeWeather() {
        val current = getCurrentWeather()
        val previous = lastKnownWeather
        if (previous == current) return
        lastKnownWeather = current
        fireWeatherChangeEvent(previous, current)
    }

    /**
     * 実効天候の変化を通知する。初回観測 (`previous == null`) は変化ではないため発火しない。
     * 非同期リフレッシュループからも呼ばれるため、スレッドに応じて async フラグを立てる。
     */
    private fun fireWeatherChangeEvent(
        previous: WeatherType?,
        current: WeatherType,
    ) {
        if (previous == null) return
        FishingWorldWeatherChangeEvent(
            worldId,
            previous,
            current,
            !Bukkit.isPrimaryThread(),
        ).callEvent()
    }

    override fun getWorldSpawnPosition(): LocationData = worldDetailConfig.spawnLocation

    /**
     * スポーン位置を設定する。設定ファイルへの保存はコアが行い、Bukkit ワールドのスポーンへの反映は
     * WorldLifecycle Integration に委ねる (未導入時は保存のみ行う)。
     */
    override fun setWorldSpawnPosition(location: LocationData) {
        val provider = plugin.getWorldLifecycleProvider()
        if (provider != null) {
            IntegrationCall.call(plugin.logger, "WorldLifecycleProvider.setSpawnLocation", Unit) {
                provider.setSpawnLocation(
                    worldKey = worldKey,
                    x = location.x,
                    y = location.y,
                    z = location.z,
                    yaw = location.yaw.toFloat(),
                    pitch = location.pitch.toFloat(),
                )
            }
        } else {
            plugin.logger.fine(
                "[${worldId.value}] spawn application skipped: WorldLifecycleProvider is not available.",
            )
        }

        if (worldDetailConfig.spawnLocation != location) {
            plugin.logger.info("[${worldId.value}] World spawn position updated: ${worldDetailConfig.spawnLocation} -> $location")
        }
        persistWorldDetailConfig(worldDetailConfig.copy(spawnLocation = location))
    }

    /**
     * Bukkit ワールドの現在のボーダーサイズを返す (読み取りのみ)。
     */
    override fun getSize(): Double =
        (Bukkit.getWorld(worldId.toWorldKey()) ?: throw IllegalStateException("World not found"))
            .worldBorder
            .size

    override fun setSize(size: Double) {
        applyBorder(worldDetailConfig.borderCentral, size)
        if (worldDetailConfig.borderSize != size) {
            plugin.logger.info("[${worldId.value}] World size updated: ${worldDetailConfig.borderSize} -> $size")
        }
        persistWorldDetailConfig(worldDetailConfig.copy(borderSize = size))
    }

    override fun getCenter(): Pair<Double, Double> = worldDetailConfig.borderCentral

    override fun setCenter(center: Pair<Double, Double>) {
        val currentSize = worldDetailConfig.borderSize ?: configManager.getConfig().world.defaultWorldSize
        applyBorder(center, currentSize)
        if (worldDetailConfig.borderCentral != center) {
            plugin.logger.info("[${worldId.value}] World center updated: ${worldDetailConfig.borderCentral} -> $center")
        }
        persistWorldDetailConfig(worldDetailConfig.copy(borderCentral = center))
    }

    /**
     * ワールド詳細設定を保存する。内容が変わらない場合はファイルへ書き込まない。
     */
    private fun persistWorldDetailConfig(newData: WorldDetailConfig) {
        if (newData == worldDetailConfig) return
        val file = pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        file.writeText(Utils.json.encodeToString(WorldDetailConfig.serializer(), newData))
        worldDetailConfig = newData
    }

    /**
     * ワールドボーダーを適用する。`enableBorder` が無効な場合、
     * または WorldLifecycle Integration が未導入の場合は何もしない。
     */
    private fun applyBorder(
        center: Pair<Double, Double>,
        size: Double,
    ) {
        if (!worldDetailConfig.enableBorder) {
            plugin.logger.fine(
                "[${worldId.value}] border application skipped: enableBorder is false.",
            )
            return
        }
        plugin.getWorldLifecycleProvider()?.applyBorder(
            worldKey = worldKey,
            centerX = center.first,
            centerZ = center.second,
            size = size,
        ) ?: plugin.logger.fine(
            "[${worldId.value}] border application skipped: WorldLifecycleProvider is not available.",
        )
    }

    /**
     * 設定 (固定時刻または実時刻) から求めたワールド時刻を、WorldLifecycle Integration 経由で適用する。
     * 未導入時は時刻を変更しない。
     */
    override fun synchronizeTime() {
        val time = calculateWorldTime(currentClimateConfig())
        val provider =
            plugin.getWorldLifecycleProvider() ?: run {
                plugin.logger.fine(
                    "[${worldId.value}] time synchronization skipped: WorldLifecycleProvider is not available.",
                )
                return
            }
        IntegrationCall.call(plugin.logger, "WorldLifecycleProvider.setTime", Unit) {
            provider.setTime(worldKey, time)
        }
    }

    /**
     * 設定から適用すべきワールド時刻 (tick) を計算する。
     */
    private fun calculateWorldTime(climateConfig: ClimateConfig): Long {
        val constantDayCycle = climateConfig.constant.dayCycle
        if (constantDayCycle != null) {
            return (constantDayCycle.toLong() * 1000 + 18000) % 24000
        }
        val timezone = configManager.getConfig().world.defaultTimeZone
        val time = LocalDateTime.now(ZoneId.of(timezone))
        val hour = time.hour + climateConfig.dayCycle.offset
        val minute = time.minute
        return ((hour * 1000 + minute * 16).toLong() + 18000) % 24000
    }

    /**
     * ワールドの状態 (ボーダー・ゲームルール・天候・時間) を設定へ同期する。
     * コア内部の非同期リフレッシュループおよびリロードから呼ばれる。
     * コアは値を決定するのみで、適用は各 Integration に委ねる。
     */
    fun updateState() {
        // 設定は変更しない (適用のみ)。永続化は setCenter/setSize 等の設定変更 API が担う。
        applyBorder(
            worldDetailConfig.borderCentral,
            worldDetailConfig.borderSize ?: configManager.getConfig().world.defaultWorldSize,
        )

        val climateConfig = currentClimateConfig()
        val source = resolveWeatherSource()

        updateGameRule(climateConfig, source)

        // 天候制御: ソースがワールド天候を駆動する場合はプロバイダーの決定を適用し、
        // 駆動しない場合は実効天候の変化検知のみ行う
        if (source.managesWorldWeather) {
            refreshManagedWeather()
        } else {
            observeWeather()
        }

        if (climateConfig.enableDayCycle) {
            synchronizeTime()
        }
    }

    /**
     * ゲームルールの値を決定し、Integration 経由で適用する。
     *
     * `DO_DAYLIGHT_CYCLE` は `enableDayCycle` の否定 (コアが時刻を同期する間はバニラの昼夜を止める) とし、
     * WorldLifecycle Integration に適用を委ねる。
     *
     * `DO_WEATHER_CYCLE` はソースの `usesVanillaWeatherCycle` に従う
     * （`moripafishing:vanilla` のみ true。バニラ天候を読み取るため）。
     * それ以外のソースでは false にして、MoripaFishing または外部プラグインの管理に委ねる。
     * 適用は Weather Integration に委ねる。
     * `applyWeatherEffects = false` の場合は一度だけバニラ既定値 (true) へ戻した後は触れず、
     * バニラや他プラグインの制御に委ねる。
     *
     * 対応する Integration が未導入の場合は何もしない。
     */
    private fun updateGameRule(
        climateConfig: ClimateConfig,
        source: WeatherSource,
    ) {
        val lifecycleProvider = plugin.getWorldLifecycleProvider()
        if (lifecycleProvider != null) {
            IntegrationCall.call(plugin.logger, "WorldLifecycleProvider.setDaylightCycle", Unit) {
                lifecycleProvider.setDaylightCycle(worldKey, !climateConfig.enableDayCycle)
            }
        } else {
            plugin.logger.fine(
                "[${worldId.value}] daylight cycle application skipped: WorldLifecycleProvider is not available.",
            )
        }

        val weatherControlProvider = plugin.getWeatherControlProvider()
        if (weatherControlProvider == null) {
            plugin.logger.fine(
                "[${worldId.value}] weather cycle application skipped: WeatherControlProvider is not available.",
            )
            return
        }
        if (climateConfig.applyWeatherEffects) {
            weatherCycleReleased = false
            IntegrationCall.call(plugin.logger, "WeatherControlProvider.setWeatherCycle", Unit) {
                weatherControlProvider.setWeatherCycle(worldKey, source.usesVanillaWeatherCycle)
            }
        } else if (!weatherCycleReleased) {
            // ゲームルールはワールドデータへ永続化されるため、過去に強制した false が
            // 残らないよう、管理をやめる際に一度だけバニラ既定値へ戻す。
            IntegrationCall.call(plugin.logger, "WeatherControlProvider.setWeatherCycle", Unit) {
                weatherControlProvider.setWeatherCycle(worldKey, true)
            }
            weatherCycleReleased = true
        }
    }

    override fun effectFinish() {
        val source = resolveWeatherSource()
        if (!source.managesWorldWeather || !currentClimateConfig().applyWeatherEffects) {
            // MoripaFishing がワールド天候を触っていない場合はリセットも行わない
            return
        }
        plugin.getWeatherControlProvider()?.resetWeather(worldKey)
        plugin.logger.info("[${worldId.value}] effectFinish called: weather reset requested.")
    }

    private fun currentClimateConfig(): ClimateConfig =
        worldDetailConfig.climateConfig
            ?: configManager.getConfig().world.defaultClimateConfig
}
