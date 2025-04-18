package party.morino.moripafishing.mocks.world

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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
    private val worldSpawnPosition: LocationData = LocationData(0.0, 64.0, 0.0, 90.0, 0.0),
    private val radius: Double = 100.0,
    private val center: Pair<Double, Double> = Pair(0.0, 0.0)
) : FishingWorld, KoinComponent {

    private var currentWeather: WeatherType = weatherType

    override fun getId(): FishingWorldId {
        return worldId
    }

    override fun getCalculatedWeather(): WeatherType {
        return weatherType
    }

    override fun getCurrentWeather(): WeatherType {
        return currentWeather
    }

    override fun setWeather(weatherType: WeatherType) {
        currentWeather = weatherType
    }

    override fun getWorldSpawnPosition(): LocationData {
        return worldSpawnPosition
    }

    override fun setWorldSpawnPosition(locationData: LocationData) {
        // モックなので何もしないのだ
    }

    override fun getRadius(): Double {
        return radius
    }

    override fun setRadius(size: Double) {
        // モックなので何もしないのだ
    }

    override fun getCenter(): Pair<Double, Double> {
        return center
    }

    override fun setCenter(x: Double, z: Double) {
        // モックなので何もしないのだ
    }

    override fun refreshSetting() {
        // モックなので何もしないのだ
    }
} 