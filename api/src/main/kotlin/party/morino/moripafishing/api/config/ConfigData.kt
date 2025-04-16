package party.morino.moripafishing.api.config

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.database.DatabaseConfig
import party.morino.moripafishing.api.config.fish.FishingConfig
import party.morino.moripafishing.api.config.world.SpawnConfig
import party.morino.moripafishing.api.config.world.WorldConfig

// プラグイン全体の設定データを保持するデータクラス
@Serializable
data class ConfigData(
    val database: DatabaseConfig = DatabaseConfig(), // データベース設定
    val world: WorldConfig = WorldConfig(), // ワールド設定
    val fishing: FishingConfig = FishingConfig(), // 釣り設定
    val spawn: SpawnConfig = SpawnConfig() // スポーン設定
)