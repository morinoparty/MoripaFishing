package party.morino.moripafishing.mocks.world

import org.koin.core.component.KoinComponent
import party.morino.moripafishing.api.config.world.WorldDetailConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * FishingWorldのモッククラスなのだ
 */
class FishingWorldMock(
    private val worldId: FishingWorldId,
    private val weatherType: WeatherType = WeatherType.SUNNY,
    private val worldSpawnPosition: LocationData = LocationData(worldId, 0.0, 64.0, 0.0, 90.0, 0.0),
    private val radius: Double = 100.0,
    private val center: Pair<Double, Double> = Pair(0.0, 0.0),
) : FishingWorld,
    KoinComponent {
    private var currentWeather: WeatherType = weatherType

    override fun getId(): FishingWorldId = worldId

    override fun getCalculatedWeather(): WeatherType = weatherType

    override fun getCurrentWeather(): WeatherType = currentWeather

    override fun setWeather(weatherType: WeatherType) {
        currentWeather = weatherType
    }

    override fun getWorldSpawnPosition(): LocationData = worldSpawnPosition

    override fun setWorldSpawnPosition(location: LocationData) {
        // モックなので何もしないのだ
    }

    override fun getSize(): Double = radius

    override fun setSize(size: Double) {
        // モックなので何もしないのだ
    }

    override fun getCenter(): Pair<Double, Double> = center

    override fun setCenter(
        x: Double,
        z: Double,
    ) {
        // モックなので何もしないのだ
    }

    override fun syncronoizeTime() {
        // モックなので何もしないのだ
    }

    override fun updateState() {
        // モックなので何もしないのだ
    }

    override fun getWorldDetails(): WorldDetailConfig = WorldDetailConfig()

    override fun loadConfig() {
        // モックなので何もしないのだ
    }

    override fun effectFinish() {
        // モックなので何もしないのだ
    }
}
