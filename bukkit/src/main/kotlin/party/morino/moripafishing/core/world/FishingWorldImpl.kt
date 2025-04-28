package party.morino.moripafishing.core.world

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
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
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.random.weather.WeatherRandomizerImpl
import party.morino.moripafishing.utils.coroutines.minecraft

@kotlinx.serialization.ExperimentalSerializationApi
class FishingWorldImpl(private val worldId: FishingWorldId) : FishingWorld, KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val pluginDirectory: PluginDirectory by inject()
    private val configManager: ConfigManager by inject()

    private lateinit var worldDetailConfig: WorldDetailConfig

    private var weatherType: WeatherType = WeatherType.SUNNY

    private val weatherRandomizer: WeatherRandomizer by lazy {
        val weatherRandomizer = WeatherRandomizerImpl(worldId)
        weatherRandomizer
    }

    init {
        loadConfig()
        // 呼び出し元
        // val t = Thread.currentThread().getStackTrace();
        plugin.logger.info("FishingWorldImpl(${worldId.value}) initialized")
        // plugin.logger.info("Thread: ${t.map { it }}")
    }

    fun loadConfig() {
        val file = pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        if (!file.exists()) {
            throw IllegalArgumentException("World detail config file not found: ${file.absolutePath}")
        }
        worldDetailConfig = Json.decodeFromStream<WorldDetailConfig>(file.inputStream())
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
                plugin.logger.info("in minecraft:World spawn position updated: $locationData")
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
        worldDetailConfig = worldDetailConfig.copy(spawnLocationData = locationData)
        val file = pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        file.outputStream().use { outputStream ->
            Json.encodeToStream(worldDetailConfig, outputStream)
        }
        plugin.logger.info("World spawn position updated: $locationData")
    }

    override fun getSize(): Double {
        return world.worldBorder.size
    }

    override fun setSize(size: Double) {
        runBlocking {
            withContext(Dispatchers.minecraft) {
                plugin.logger.info("in minecraft:World border size updated: $size")
                world.worldBorder.size = size
            }
        }
        worldDetailConfig = worldDetailConfig.copy(borderSize = size)
        val file = pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        file.outputStream().use { outputStream ->
            Json.encodeToStream(worldDetailConfig, outputStream)
        }
        plugin.logger.info("World border size updated: $size")
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
                plugin.logger.info("in minecraft:World border center updated: ($x, $z)")
                val worldBorder = world.worldBorder
                worldBorder.center = Location(world, x, 0.0, z)
            }
        }
        worldDetailConfig = worldDetailConfig.copy(borderCentral = Pair(x, z))
        val file = pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")
        file.outputStream().use { outputStream ->
            Json.encodeToStream(worldDetailConfig, outputStream)
        }
        plugin.logger.info("World border center updated: ($x, $z)")
    }

    override fun updateState() {
        updateWeather()
    }

    private fun updateGameRule() {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
    }
}
