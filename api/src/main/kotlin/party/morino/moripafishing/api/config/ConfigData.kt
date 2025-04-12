package party.morino.moripafishing.api.config

import party.morino.moripafishing.api.model.Rarity
data class ConfigData(
    val database: DatabaseConfig = DatabaseConfig(),
    val world: WorldConfig = WorldConfig(),
    val weather: WeatherConfig = WeatherConfig(),
    val fishing: FishingConfig = FishingConfig()
    )

data class WorldConfig(
    val defaultId: String = "default",
    val list: List<WorldDetailConfig> = listOf(WorldDetailConfig()),
    val spawnMobs: Boolean = false,
    val spawnMonsters: Boolean = false,
    val spawnAnimals: Boolean = false,
    val receiveDamage: Boolean = false,
)

data class WorldDetailConfig(
    val id: String = "default",
    val name: String = "つりとぴあ",
    val radius: Int = 100,

)

data class WeatherConfig(
    val weatherNoise : String = "Perlin",
    val dayCycleTimeZone: String = "Asia/Tokyo",
)


data class DatabaseConfig(
    val type: String = "SQLITE",
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "moripafishing",
    val username: String = "root",
    val password: String = "password"
)
/**
 * 釣りの確率を設定するクラス
 * @param percentage 各レアリティの確率(0.0~100.0)。負の値の場合は、正の値で定義されたものの余剰分から計算される。
 */
data class FishingConfig(
    val percentage : Map<Rarity, Double> = mapOf(Rarity.COMMON to -1.0, Rarity.RARE to 20.0, Rarity.EPIC to 6.5, Rarity.LEGENDARY to 0.4, Rarity.MYTHIC to 0.1, Rarity.JUNK to 1.0)
)