package party.morino.moripafishing.api.config

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.api.model.world.FishingWorldId

// プラグイン全体の設定データを保持するデータクラス
@Serializable
data class ConfigData(
    val database: DatabaseConfig = DatabaseConfig(), // データベース設定
    val world: WorldConfig = WorldConfig(), // ワールド設定
    val defaultWeatherConfig: WeatherConfig = WeatherConfig(), // 天候設定
    val fishing: FishingConfig = FishingConfig(), // 釣り設定
    val spawn: SpawnConfig = SpawnConfig() // スポーン設定
)

// ワールドに関する設定を保持するデータクラス
@Serializable
data class WorldConfig(
    val defaultId: FishingWorldId = FishingWorldId("default"), // デフォルトのワールドID
    val list: List<WorldDetailConfig> = listOf(WorldDetailConfig()), // ワールドの詳細設定リスト
    val spawnConfig: SpawnConfig = SpawnConfig(), // スポーン設定
    val defaultWorldRadius : Int = 100
)

// ワールドの詳細設定を保持するデータクラス
@Serializable
data class WorldDetailConfig(
    val id:  @Serializable FishingWorldId = FishingWorldId("default"), // ワールドのID
    val name: String = "つりとぴあ", // ワールドの名前
    val worldGenerator : String = "Terra:OVERWORLD", // ワールドの生成器
    val radius: Int? = null,
    val weatherConfig : WeatherConfig? = null
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

/**
 * スポーン関連の設定を管理するクラス
 * @param spawnMobs モブをスポーンさせるかどうか
 * @param spawnMonsters モンスターをスポーンさせるかどうか
 * @param spawnAnimals 動物をスポーンさせるかどうか
 * @param receiveDamage ダメージを受けるかどうか
 */
@Serializable
data class SpawnConfig(
    val spawnMobs: Boolean = false,
    val spawnMonsters: Boolean = false,
    val spawnAnimals: Boolean = false,
    val receiveDamage: Boolean = false
) 