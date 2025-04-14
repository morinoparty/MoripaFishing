package party.morino.moripafishing.api.config

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.WeatherType

// プラグイン全体の設定データを保持するデータクラス
@Serializable
data class ConfigData(
    val database: DatabaseConfig = DatabaseConfig(), // データベース設定
    val world: WorldConfig = WorldConfig(), // ワールド設定
    val weather: WeatherConfig = WeatherConfig(), // 天候設定
    val fishing: FishingConfig = FishingConfig() // 釣り設定
)

// ワールドに関する設定を保持するデータクラス
@Serializable
data class WorldConfig(
    val defaultId: String = "default", // デフォルトのワールドID
    val list: List<WorldDetailConfig> = listOf(WorldDetailConfig()), // ワールドの詳細設定リスト
    val spawnMobs: Boolean = false, // モブをスポーンさせるかどうか
    val spawnMonsters: Boolean = false, // モンスターをスポーンさせるかどうか
    val spawnAnimals: Boolean = false, // 動物をスポーンさせるかどうか
    val receiveDamage: Boolean = false, // ダメージを受けるかどうか
    val defaultWorldRadius : Int = 100
)

// ワールドの詳細設定を保持するデータクラス
@Serializable
data class WorldDetailConfig(
    val id: String = "default", // ワールドのID
    val name: String = "つりとぴあ", // ワールドの名前
    val radius: Int = 100 // ワールドの半径
)

// 天候に関する設定を保持するデータクラス
@Serializable
data class WeatherConfig(
    val dayCycleTimeZone: String = "Asia/Tokyo", // 日周期のタイムゾーン
    val interval: Int = 8, // 天候の更新間隔
    val offset: Int = 0, // 天候のオフセット
    val frequency: Double = 0.15, // perlin noiseの周波数 0.15=delta 0.2 0.30=delta 0.37 0.45=delta 0.54
    val weatherSetting: Map<WeatherType, Int> = mapOf(
        WeatherType.SUNNY to 30,
        WeatherType.CLOUDY to 20,
        WeatherType.RAINY to 20,
        WeatherType.THUNDERSTORM to 10
    ),
    val hashPepper: String = "hashPepper"
)

// データベースに関する設定を保持するデータクラス
@Serializable
data class DatabaseConfig(
    val type: String = "SQLITE", // データベースの種類
    val host: String = "localhost", // データベースのホスト
    val port: Int = 3306, // データベースのポート
    val database: String = "moripafishing", // データベース名
    val username: String = "root", // データベースのユーザー名
    val password: String = "password" // データベースのパスワード
)

/**
 * 釣りの設定を保持するデータクラス
 */
@Serializable
data class FishingConfig(
    val test: String = "test"
)