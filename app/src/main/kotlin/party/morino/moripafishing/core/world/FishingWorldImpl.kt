package party.morino.moripafishing.core.world

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.Location
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.config.world.WorldDetailConfig
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.utils.coroutines.minecraft
import party.morino.moripafishing.api.config.ConfigManager

class FishingWorldImpl(private val worldId: FishingWorldId) : FishingWorld, KoinComponent {
    private val plugin : MoripaFishing by inject()
    private val randomizer: RandomizeManager by inject()
    private val worldManager : WorldManager by inject()
    private val configManager : ConfigManager by inject()
    private val worldDetailConfig : WorldDetailConfig by lazy{
        worldManager.getWorldDetails(worldId) ?: throw IllegalStateException("WorldDetailConfig not found for worldId: $worldId")
    }
    private var weatherRandomizer: WeatherRandomizer = randomizer.getWeatherRandomizer()

    lateinit var weatherType: WeatherType

    var world : World = Bukkit.getWorld(NamespacedKey(plugin, worldId.value)) ?: throw IllegalStateException("World not found")

    init {
        weatherRandomizer.setSeedWithWorldId(worldId)
        updateWeather()
    }


    override fun getId(): FishingWorldId {
        return worldId
    }

    override fun getCalculatedWeather(): WeatherType {
        return weatherRandomizer.getWeather(worldId)
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
                world.spawnLocation = Location(
                    world,
                    locationData.x,
                    locationData.y,
                    locationData.z,
                    locationData.yaw.toFloat(),
                    locationData.pitch.toFloat()
                )
            }
        }
        //TODO configに保存
    }

    override fun getRadius(): Double {
        return world.worldBorder.size
    }

    override fun setRadius(size: Double) {
        world.worldBorder.size = size
        //TODO configに保存
    }

    override fun getCenter(): Pair<Double, Double> {
        return worldDetailConfig.borderCentral
    }

    override fun setCenter(x: Double, z: Double) {
        world.worldBorder.setCenter(x, z)
        //TODO configに保存
    }

    override fun refreshSetting() {
        setCenter(worldDetailConfig.borderCentral.first, worldDetailConfig.borderCentral.second)
        setRadius(worldDetailConfig.borderSize ?: configManager.getConfig().world.defaultWorldSize)
        setWeather(getCalculatedWeather())
    }
}
