package party.morino.moripafishing.core.world

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.decodeFromStream
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.config.world.WorldDetailConfig
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.utils.Utils
import party.morino.moripafishing.utils.coroutines.minecraft
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.io.writeText

@kotlinx.serialization.ExperimentalSerializationApi
class FishingWorldImpl(private val worldId: FishingWorldId) : FishingWorld, KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val pluginDirectory: PluginDirectory by inject()
    private val configManager: ConfigManager by inject()
    private val randomizeManager: RandomizeManager by inject()

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

    var world: World =
        lazy {
            Bukkit.getWorld(worldId.value)
                ?: throw IllegalStateException("World not found")
        }.value

    override fun getWorldDetails(): WorldDetailConfig {
        return worldDetailConfig
    }

    override fun getId(): FishingWorldId {
        return worldId
    }

    override fun getCalculatedWeather(): WeatherType {
        return weatherRandomizer.drawWeather()
    }

    override fun getCurrentWeather(): WeatherType {
        return weatherType
    }

    override fun setWeather(weatherType: WeatherType) {
        runBlocking {
            withContext(Dispatchers.minecraft) {
                when (weatherType) {
                    WeatherType.SUNNY -> {
                        world.setStorm(false)
                        world.isThundering = false
                    }

                    WeatherType.CLOUDY -> {
                        // TODO 置き換え
                        world.setStorm(false)
                        world.isThundering = true
                    }

                    WeatherType.RAINY -> {
                        world.setStorm(true)
                        world.isThundering = false
                    }

                    WeatherType.THUNDERSTORM -> {
                        world.setStorm(true)
                        world.isThundering = true
                    }

                    else -> {}
                }
            }
        }
        this.weatherType = weatherType
    }

    override fun getWorldSpawnPosition(): LocationData {
        return worldDetailConfig.spawnLocationData
    }

    override fun setWorldSpawnPosition(locationData: LocationData) {
        runBlocking {
            withContext(Dispatchers.minecraft) {
                world.spawnLocation =
                    Location(
                        world,
                        locationData.x,
                        locationData.y,
                        locationData.z,
                        locationData.yaw.toFloat(),
                        locationData.pitch.toFloat(),
                    )
            }
        }

        val file =
            pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        val newData = worldDetailConfig.copy(spawnLocationData = locationData)
        file.writeText(Utils.json.encodeToString(WorldDetailConfig.serializer(), newData))
        if (worldDetailConfig.spawnLocationData != locationData) {
            plugin.logger.info("[${worldId.value}] World spawn position updated: ${worldDetailConfig.spawnLocationData} -> $locationData")
        }
        worldDetailConfig = newData
    }

    override fun getSize(): Double {
        return world.worldBorder.size
    }

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

    override fun getCenter(): Pair<Double, Double> {
        return worldDetailConfig.borderCentral
    }

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
        setCenter(worldDetailConfig.borderCentral.first, worldDetailConfig.borderCentral.second)
        setSize(worldDetailConfig.borderSize ?: configManager.getConfig().world.defaultWorldSize)
        updateGameRule()
        updateWeather()
        syncronoizeTime()
    }

    private fun updateGameRule() {
        runBlocking {
            withContext(Dispatchers.minecraft) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            }
        }
    }
}
