package party.morino.moripafishing.api.config.world

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.weather.WeatherConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.utils.serializer.FishingWorldIdSerializer

// ワールドに関する設定を保持するデータクラス
@Serializable
data class WorldConfig(
    // デフォルトのワールドID
    val defaultId:
        @Serializable(with = FishingWorldIdSerializer::class)
        FishingWorldId = FishingWorldId("default"),
    // スポーン設定
    val spawnConfig: SpawnConfig = SpawnConfig(),
    // デフォルトのワールドサイズ
    val defaultWorldSize: Double = 100.0,
    // デフォルトのワールドジェネレータ
    val defaultWorldGenerator: String = "Terra:OVERWORLD",
    // デフォルトのワールドバイオーム
    val defaultWorldBiome: String? = null,
    // デフォルトの天気設定
    val defaultWeatherConfig: WeatherConfig = WeatherConfig(),
    // 更新の感覚
    val refreshInterval: Int = 60,
    // timezone
    val defaultTimeZone: String = "Asia/Tokyo"
)
