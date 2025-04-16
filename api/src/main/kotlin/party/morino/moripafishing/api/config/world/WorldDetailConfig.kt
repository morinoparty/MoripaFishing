package party.morino.moripafishing.api.config.world

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.config.weather.WeatherConfig

// ワールドの詳細設定を保持するデータクラス
@Serializable
data class WorldDetailConfig(
    val id:  @Serializable FishingWorldId = FishingWorldId("default"), // ワールドのID
    val name: String = "つりとぴあ", // ワールドの名前
    val borderSize: Double? = null,
    val borderCentral: Pair<Double, Double> = Pair(0.0, 0.0),
    val spawnLocationData: LocationData = LocationData(0.0, 64.0, 0.0, 90.0, 0.0), // スポーン地点の座標
    val weatherConfig : WeatherConfig? = null
) 