package party.morino.moripafishing.core.world

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.decodeFromStream
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.config.climate.WeatherMode
import party.morino.moripafishing.api.config.world.WorldDetailConfig
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WeatherEffect
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.world.weather.WeatherProviderRegistry
import party.morino.moripafishing.core.world.weather.provider.ExternalWeatherProvider
import party.morino.moripafishing.core.world.weather.provider.InternalWeatherProvider
import party.morino.moripafishing.core.world.weather.provider.VanillaWeatherProvider
import party.morino.moripafishing.utils.Utils
import party.morino.moripafishing.utils.coroutines.minecraft
import java.time.LocalDateTime
import java.time.ZoneId

@kotlinx.serialization.ExperimentalSerializationApi
class FishingWorldImpl(
    private val worldId: FishingWorldId,
) : FishingWorld,
    KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val pluginDirectory: PluginDirectory by inject()
    private val configManager: ConfigManager by inject()
    private val randomizeManager: RandomizeManager by inject()
    private val weatherProviderRegistry: WeatherProviderRegistry by inject()

    private var weatherEffect: WeatherEffect = WeatherTypeRegistry.getEffect(WeatherType.SUNNY)

    private lateinit var worldDetailConfig: WorldDetailConfig

    /**
     * INTERNAL モードで MoripaFishing が自ら適用した最新の天候。
     * VANILLA / EXTERNAL モードではプロバイダーを直接参照するため使用しない。
     *
     * 非同期リフレッシュループ (`updateState`) とメインスレッド読み取り
     * (`FishRandomizerImpl` 等) の双方から触られるので `@Volatile` で可視性を担保する。
     */
    @Volatile
    private var appliedInternalWeather: WeatherType? = null

    private val weatherRandomizer: WeatherRandomizer by lazy {
        randomizeManager.getWeatherRandomizer(worldId)
    }

    @Volatile
    private var weatherProvider: WeatherProvider? = null
    private val weatherProviderLock = Any()

    /**
     * プロバイダーはモードに応じた唯一のインスタンスを保持する必要があるため
     * (例: `VanillaWeatherProvider` は Bukkit Listener を登録するので二重登録を避ける)、
     * double-checked locking で初期化を直列化する。
     */
    private fun weatherProvider(): WeatherProvider {
        val cached = weatherProvider
        if (cached != null) return cached
        return synchronized(weatherProviderLock) {
            weatherProvider ?: resolveWeatherProvider().also { weatherProvider = it }
        }
    }

    init {
        loadConfig()
        plugin.logger.info("FishingWorldImpl(${worldId.value}) initialized")
    }

    override fun loadConfig() {
        val file = pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        if (!file.exists()) {
            throw IllegalArgumentException("World detail config file not found: ${file.absolutePath}")
        }
        worldDetailConfig = Utils.json.decodeFromStream<WorldDetailConfig>(file.inputStream())
        // モードが変わり得るのでプロバイダーキャッシュを無効化する。
        // 既存の provider が Bukkit Listener を登録していた場合 (VANILLA) は解除する。
        synchronized(weatherProviderLock) {
            (weatherProvider as? VanillaWeatherProvider)?.dispose()
            weatherProvider = null
        }
    }

    private var world: World =
        lazy {
            Bukkit.getWorld(worldId.value)
                ?: throw IllegalStateException("World not found")
        }.value

    override fun getWorldDetails(): WorldDetailConfig = worldDetailConfig

    override fun getId(): FishingWorldId = worldId

    override fun getCalculatedWeather(): WeatherType = weatherRandomizer.drawWeather()

    override fun getCurrentWeather(): WeatherType = weatherProvider().getCurrentWeather(worldId)

    /**
     * 天候を設定する。
     *
     * INTERNAL モードでは内部状態を更新し Bukkit ワールドにも反映する。
     * VANILLA / EXTERNAL モードでは MoripaFishing がワールドを支配しないため no-op。
     */
    override fun setWeather(weatherType: WeatherType) {
        val mode = currentClimateConfig().weatherMode
        if (mode != WeatherMode.INTERNAL) {
            plugin.logger.fine(
                "[${worldId.value}] setWeather($weatherType) ignored: weatherMode=$mode is not INTERNAL.",
            )
            return
        }
        if (weatherType == this.appliedInternalWeather) {
            return
        }
        this.appliedInternalWeather = weatherType
        this.weatherEffect.reset()
        val newEffect = WeatherTypeRegistry.getEffect(weatherType)
        this.weatherEffect = newEffect
        this.weatherEffect.apply(fishingWorldId = worldId)
    }

    override fun getWorldSpawnPosition(): LocationData = worldDetailConfig.spawnLocation

    override fun setWorldSpawnPosition(location: LocationData) {
        runBlocking {
            withContext(Dispatchers.minecraft) {
                world.spawnLocation =
                    org.bukkit.Location(
                        world,
                        location.x,
                        location.y,
                        location.z,
                        location.yaw.toFloat(),
                        location.pitch.toFloat(),
                    )
            }
        }

        val file =
            pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        val newData = worldDetailConfig.copy(spawnLocation = location)
        file.writeText(Utils.json.encodeToString(WorldDetailConfig.serializer(), newData))
        if (worldDetailConfig.spawnLocation != location) {
            plugin.logger.info("[${worldId.value}] World spawn position updated: ${worldDetailConfig.spawnLocation} -> $location")
        }
        worldDetailConfig = newData
    }

    override fun getSize(): Double = world.worldBorder.size

    override fun setSize(size: Double) {
        val newCenter = worldDetailConfig.borderCentral
        plugin.getWorldLifecycleProvider()?.applyBorder(
            worldId = worldId,
            centerX = newCenter.first,
            centerZ = newCenter.second,
            size = size,
        ) ?: plugin.logger.fine(
            "[${worldId.value}] setSize($size) skipped: WorldLifecycleProvider is not available.",
        )
        val file =
            pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        val newData = worldDetailConfig.copy(borderSize = size)
        file.writeText(Utils.json.encodeToString(WorldDetailConfig.serializer(), newData))
        if (worldDetailConfig.borderSize != size) {
            plugin.logger.info("[${worldId.value}] World size updated: ${worldDetailConfig.borderSize} -> $size")
        }
        worldDetailConfig = newData
    }

    override fun getCenter(): Pair<Double, Double> = worldDetailConfig.borderCentral

    override fun setCenter(
        x: Double,
        z: Double,
    ) {
        val currentSize = worldDetailConfig.borderSize ?: configManager.getConfig().world.defaultWorldSize
        plugin.getWorldLifecycleProvider()?.applyBorder(
            worldId = worldId,
            centerX = x,
            centerZ = z,
            size = currentSize,
        ) ?: plugin.logger.fine(
            "[${worldId.value}] setCenter($x, $z) skipped: WorldLifecycleProvider is not available.",
        )
        val file =
            pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        val newData = worldDetailConfig.copy(borderCentral = Pair(x, z))
        file.writeText(Utils.json.encodeToString(WorldDetailConfig.serializer(), newData))
        if (worldDetailConfig.borderCentral != Pair(x, z)) {
            plugin.logger.info("[${worldId.value}] World center updated: ${worldDetailConfig.borderCentral} -> $x, $z")
        }
        worldDetailConfig = newData
    }

    override fun syncronoizeTime() {
        val timezone = configManager.getConfig().world.defaultTimeZone
        val zone = ZoneId.of(timezone)
        val time = LocalDateTime.now(zone)
        val climateConfig = currentClimateConfig()
        val offset: Int = climateConfig.dayCycle.offset
        val hour = time.hour + offset
        val minute = time.minute
        runBlocking {
            withContext(Dispatchers.minecraft) {
                if (climateConfig.constant.dayCycle != null) {
                    world.time = (climateConfig.constant.dayCycle!!.toLong() * 1000 + 18000) % 24000
                } else {
                    world.time = ((hour * 1000 + minute * 16).toLong() + 18000) % 24000
                }
            }
        }
    }

    override fun updateState() {
        if (worldDetailConfig.enableBorder) {
            setCenter(worldDetailConfig.borderCentral.first, worldDetailConfig.borderCentral.second)
            setSize(worldDetailConfig.borderSize ?: configManager.getConfig().world.defaultWorldSize)
        }

        val climateConfig = currentClimateConfig()

        updateGameRule(climateConfig)

        // 天候制御: INTERNAL モード時のみプラグインが天候を決定・適用する
        if (climateConfig.weatherMode == WeatherMode.INTERNAL) {
            updateWeather()
        }

        if (climateConfig.enableDayCycle) {
            syncronoizeTime()
        }
    }

    /**
     * ゲームルールを設定する。
     *
     * `DO_WEATHER_CYCLE` は VANILLA モードのみ true（バニラ天候を読み取るため）、
     * INTERNAL / EXTERNAL では false（それぞれ MoripaFishing / 外部プラグインが管理するため、
     * バニラの自動変化を止める）。
     */
    private fun updateGameRule(climateConfig: ClimateConfig) {
        runBlocking {
            withContext(Dispatchers.minecraft) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, !climateConfig.enableDayCycle)
                world.setGameRule(
                    GameRule.DO_WEATHER_CYCLE,
                    climateConfig.weatherMode == WeatherMode.VANILLA,
                )
            }
        }
    }

    override fun effectFinish() {
        weatherEffect.reset()
        plugin.logger.info("[${worldId.value}] effectFinish called: weatherEffect reset.")
    }

    private fun currentClimateConfig(): ClimateConfig =
        worldDetailConfig.climateConfig
            ?: configManager.getConfig().world.defaultClimateConfig

    private fun resolveWeatherProvider(): WeatherProvider =
        when (currentClimateConfig().weatherMode) {
            WeatherMode.INTERNAL ->
                InternalWeatherProvider(weatherRandomizer) { appliedInternalWeather }
            WeatherMode.VANILLA -> VanillaWeatherProvider(plugin)
            WeatherMode.EXTERNAL -> ExternalWeatherProvider(weatherProviderRegistry, plugin.logger)
        }
}
