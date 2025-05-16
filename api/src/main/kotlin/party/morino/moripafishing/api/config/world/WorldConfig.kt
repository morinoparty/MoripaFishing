package party.morino.moripafishing.api.config.world

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.climate.ClimateConfig
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
    // デフォルトの天気設定
    val defaultClimateConfig: ClimateConfig = ClimateConfig(),
    // 更新の感覚
    val refreshInterval: Int = 60,
    // timezone
    val defaultTimeZone: String = "Asia/Tokyo"
)
