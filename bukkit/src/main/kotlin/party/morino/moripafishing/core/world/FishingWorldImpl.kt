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
import party.morino.moripafishing.api.config.world.WorldDetailConfig
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WeatherEffect
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType
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

    private var weatherEffect: WeatherEffect = WeatherTypeRegistry.getEffect(WeatherType.SUNNY)

    private lateinit var worldDetailConfig: WorldDetailConfig

    private var weatherType: WeatherType = WeatherType.SUNNY

    private val weatherRandomizer: WeatherRandomizer by lazy {
        val weatherRandomizer = randomizeManager.getWeatherRandomizer(worldId)
        weatherRandomizer
    }

    init {
        loadConfig()
        // 呼び出し元
        // val t = Thread.currentThread().getStackTrace();
        plugin.logger.info("FishingWorldImpl(${worldId.value}) initialized")
        // plugin.logger.info("Thread: ${t.map { it }}")
    }

    override fun loadConfig() {
        val file = pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        if (!file.exists()) {
            throw IllegalArgumentException("World detail config file not found: ${file.absolutePath}")
        }
        worldDetailConfig = Utils.json.decodeFromStream<WorldDetailConfig>(file.inputStream())
    }

    private var world: World =
        lazy {
            Bukkit.getWorld(worldId.value)
                ?: throw IllegalStateException("World not found")
        }.value

    override fun getWorldDetails(): WorldDetailConfig = worldDetailConfig

    override fun getId(): FishingWorldId = worldId

    override fun getCalculatedWeather(): WeatherType = weatherRandomizer.drawWeather()

    override fun getCurrentWeather(): WeatherType = weatherType

    override fun setWeather(weatherType: WeatherType) {
        if (weatherType == this.weatherType) {
            return
        }
        this.weatherType = weatherType
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
        runBlocking {
            withContext(Dispatchers.minecraft) {
                world.worldBorder.size = size
            }
        }
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
        runBlocking {
            withContext(Dispatchers.minecraft) {
                world.worldBorder.setCenter(x, z)
            }
        }
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
        val climateConfig = worldDetailConfig.climateConfig ?: configManager.getConfig().world.defaultClimateConfig
        val offset: Int = climateConfig.dayCycle.offset
        val hour = time.hour + offset
        val minute = time.minute
        runBlocking {
            withContext(Dispatchers.minecraft) {
                if (climateConfig.constant.dayCycle != null) {
                    world.time = (climateConfig.constant.dayCycle!!.toLong() * 1000 + 18000) % 24000
                } else {
                    // 0 で 6:00
                    // 1000 で 7:00
                    world.time = ((hour * 1000 + minute * 16).toLong() + 18000) % 24000
                }
            }
        }
    }

    override fun updateState() {
        // ボーダー管理: enableBorder が true の場合のみプラグインがボーダーを制御する
        if (worldDetailConfig.enableBorder) {
            setCenter(worldDetailConfig.borderCentral.first, worldDetailConfig.borderCentral.second)
            setSize(worldDetailConfig.borderSize ?: configManager.getConfig().world.defaultWorldSize)
        }

        val climateConfig =
            worldDetailConfig.climateConfig
                ?: configManager.getConfig().world.defaultClimateConfig

        updateGameRule(climateConfig)

        // 天候制御: enableWeather が true の場合のみプラグインが天候を管理する
        if (climateConfig.enableWeather) {
            updateWeather()
        }

        // 時間同期: enableDayCycle が true の場合のみプラグインが時間を管理する
        if (climateConfig.enableDayCycle) {
            syncronoizeTime()
        }
    }

    /**
     * ゲームルールを設定する
     *
     * 各フラグが有効な場合はバニラのサイクルを無効化し、プラグインが制御する。
     * 無効な場合はバニラのサイクルを有効化し、Minecraft のデフォルト動作に委ねる。
     */
    private fun updateGameRule(climateConfig: ClimateConfig) {
        runBlocking {
            withContext(Dispatchers.minecraft) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, !climateConfig.enableDayCycle)
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, !climateConfig.enableWeather)
            }
        }
    }

    /**
     * 天候や特殊効果の終了時に呼び出されるメソッド
     * ここでは天候効果のリセットや、必要な後処理を実装する
     */
    override fun effectFinish() {
        // 現在の天候効果をリセット
        weatherEffect.reset()
        // 必要に応じて追加の後処理をここに記述
        plugin.logger.info("[${worldId.value}] effectFinish called: weatherEffect reset.")
    }
}
