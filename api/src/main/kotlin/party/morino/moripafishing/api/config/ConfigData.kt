package party.morino.moripafishing.api.config

import party.morino.moripafishing.api.model.Rarity
import party.morino.moripafishing.api.model.WeatherType
// プラグイン全体の設定データを保持するデータクラス
data class ConfigData(
    val database: DatabaseConfig = DatabaseConfig(), // データベース設定
    val world: WorldConfig = WorldConfig(), // ワールド設定
    val weather: WeatherConfig = WeatherConfig(), // 天候設定
    val fishing: FishingConfig = FishingConfig() // 釣り設定
)

// ワールドに関する設定を保持するデータクラス
data class WorldConfig(
    val defaultId: String = "default", // デフォルトのワールドID
    val list: List<WorldDetailConfig> = listOf(WorldDetailConfig()), // ワールドの詳細設定リスト
    val spawnMobs: Boolean = false, // モブをスポーンさせるかどうか
    val spawnMonsters: Boolean = false, // モンスターをスポーンさせるかどうか
    val spawnAnimals: Boolean = false, // 動物をスポーンさせるかどうか
    val receiveDamage: Boolean = false // ダメージを受けるかどうか
)

// ワールドの詳細設定を保持するデータクラス
data class WorldDetailConfig(
    val id: String = "default", // ワールドのID
    val name: String = "つりとぴあ", // ワールドの名前
    val radius: Int = 100 // ワールドの半径
)

// 天候に関する設定を保持するデータクラス
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
    )
)

// データベースに関する設定を保持するデータクラス
data class DatabaseConfig(
    val type: String = "SQLITE", // データベースの種類
    val host: String = "localhost", // データベースのホスト
    val port: Int = 3306, // データベースのポート
    val database: String = "moripafishing", // データベース名
    val username: String = "root", // データベースのユーザー名
    val password: String = "password" // データベースのパスワード
)

/**
 * 釣りの確率を設定するクラス
 * @param percentage 各レアリティの確率(0.0~100.0)。負の値の場合は、正の値で定義されたものの余剰分から計算される。
 */
data class FishingConfig(
    val percentage: Map<Rarity, Double> = mapOf(
        Rarity.COMMON to -1.0, // 一般的なレアリティの確率
        Rarity.RARE to 20.0, // レアなレアリティの確率
        Rarity.EPIC to 6.5, // エピックなレアリティの確率
        Rarity.LEGENDARY to 0.4, // 伝説的なレアリティの確率
        Rarity.MYTHIC to 0.1, // 神話的なレアリティの確率
        Rarity.JUNK to 1.0 // ジャンクアイテムの確率
    )
)