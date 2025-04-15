package party.morino.moripafishing.core.world

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType

class FishingWorldImpl(private val worldId: FishingWorldId) : FishingWorld, KoinComponent {
    private val plugin : MoripaFishing by inject()
    private val randomizer: RandomizeManager by inject()
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
        this.weatherType = weatherType
    }


    override fun getWorldSpawnPosition(): LocationData {
        TODO("Not yet implemented")
    }

    override fun getRadius(): Int {
        TODO("Not yet implemented")
    }

    override fun getCenter(): LocationData {
        TODO("Not yet implemented")
    }
}
