package party.morino.moripafishing.core.world

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.decodeFromStream
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.World
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
import party.morino.moripafishing.core.world.weather.provider.InternalWeatherProvider
import party.morino.moripafishing.core.world.weather.provider.VanillaWeatherProvider
import party.morino.moripafishing.utils.Utils
import party.morino.moripafishing.utils.coroutines.minecraft
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

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
     * 未登録のためフォールバックしたキーの警告を1度だけ出すための記録。
     */
    private val warnedMissingSource: MutableSet<Key> = ConcurrentHashMap.newKeySet()

    /**
     * `ClimateConfig.weatherSource` のキーから `WeatherSource` を解決する。
     *
     * プロバイダーはソースごとの唯一のインスタンスを保持する必要があるため
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
        (weatherProvider as? VanillaWeatherProvider)?.dispose()
        resolvedSource = source
        weatherProvider = source.createProvider(worldId)
    }

    private fun weatherProvider(): WeatherProvider {
        resolveWeatherSource()
        return weatherProvider ?: error("Weather provider is not initialized for ${worldId.value}")
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
        // ソースが変わり得るのでプロバイダーキャッシュを無効化する。
        // 既存の provider が Bukkit Listener を登録していた場合 (VANILLA) は解除する。
        synchronized(weatherProviderLock) {
            (weatherProvider as? VanillaWeatherProvider)?.dispose()
            weatherProvider = null
            resolvedSource = null
            warnedMissingSource.clear()
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
     * ソースがワールド天候を駆動する場合 (`managesWorldWeather == true`、`moripafishing:internal` 相当) は
     * 内部状態を更新し Bukkit ワールドにも反映する。それ以外のソースでは MoripaFishing がワールドを
     * 支配しないため no-op。
     */
    override fun setWeather(weatherType: WeatherType) {
        val source = resolveWeatherSource()
        if (!source.managesWorldWeather) {
            plugin.logger.fine(
                "[${worldId.value}] setWeather($weatherType) ignored: weatherSource '${source.key}' does not manage world weather.",
            )
            return
        }
        // 内蔵プロバイダーは適用済み天候をキャッシュする。値が変わらなければ Bukkit への再適用を省く。
        val provider = weatherProvider
        if (provider is InternalWeatherProvider && !provider.applyWeather(weatherType)) {
            return
        }
        plugin.getWeatherControlProvider()?.applyWeather(
            worldId = worldId.value,
            weatherType = weatherType.name,
        ) ?: plugin.logger.fine(
            "[${worldId.value}] setWeather($weatherType) skipped: WeatherControlProvider is not available.",
        )
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
            worldId = worldId.value,
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
            worldId = worldId.value,
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
        val source = resolveWeatherSource()

        updateGameRule(climateConfig, source)

        // 天候制御: ソースがワールド天候を駆動する場合のみプラグインが天候を決定・適用する
        if (source.managesWorldWeather) {
            updateWeather()
        }

        if (climateConfig.enableDayCycle) {
            syncronoizeTime()
        }
    }

    /**
     * ゲームルールを設定する。
     *
     * `DO_WEATHER_CYCLE` はソースの `usesVanillaWeatherCycle` に従う
     * （`moripafishing:vanilla` のみ true。バニラ天候を読み取るため）。
     * それ以外のソースでは false にして、MoripaFishing または外部プラグインの管理に委ねる。
     */
    private fun updateGameRule(
        climateConfig: ClimateConfig,
        source: WeatherSource,
    ) {
        runBlocking {
            withContext(Dispatchers.minecraft) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, !climateConfig.enableDayCycle)
                world.setGameRule(
                    GameRule.DO_WEATHER_CYCLE,
                    source.usesVanillaWeatherCycle,
                )
            }
        }
    }

    override fun effectFinish() {
        plugin.getWeatherControlProvider()?.resetWeather(worldId.value)
        plugin.logger.info("[${worldId.value}] effectFinish called: weather reset requested.")
    }

    private fun currentClimateConfig(): ClimateConfig =
        worldDetailConfig.climateConfig
            ?: configManager.getConfig().world.defaultClimateConfig
}
