package party.morino.moripafishing.api.config.world

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.weather.WeatherConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.utils.serializer.FishingWorldIdSerializer

// ワールドに関する設定を保持するデータクラス
@Serializable
data class WorldConfig(
    val defaultId: @Serializable(with = FishingWorldIdSerializer::class) FishingWorldId = FishingWorldId("default"), // デフォルトのワールドID
    val spawnConfig: SpawnConfig = SpawnConfig(), // スポーン設定
    val defaultWorldSize: Double = 100.0,
    val defaultWorldGenerator: String = "Terra:OVERWORLD",
    val defaultWorldBiome: String? = null,
    val defaultWeatherConfig: WeatherConfig = WeatherConfig(),
) 