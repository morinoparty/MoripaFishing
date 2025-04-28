package party.morino.moripafishing.core.random.weather

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.utils.XorShiftRandom
import java.security.MessageDigest
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.floor
import party.morino.moripafishing.api.config.climate.ClimateConfig

/**
 * 天気をランダムに生成する実装クラス
 */
class WeatherRandomizerImpl(val fishingWorldId: FishingWorldId) : WeatherRandomizer, KoinComponent {
    private val configManager: ConfigManager by inject()
    private val worldManager: WorldManager by inject()

    private fun getClimateConfig(): ClimateConfig {
        return worldManager.getWorld(fishingWorldId).getWorldDetails().climateConfig
            ?: configManager.getConfig().world.defaultClimateConfig
    }

    private val startDate: ZonedDateTime by lazy {
        ZonedDateTime.of(
            2023,
            10,
            1,
            0,
            0,
            0,
            0,
            ZoneId.of(configManager.getConfig().world.defaultTimeZone),
        )
    }

    /**
     * 現在の天気を取得する
     * @return 現在の天気
     */
    override fun drawWeather(): WeatherType {
        val now = ZonedDateTime.now()
        return getWeatherByDate(now)
    }

    /**
     * 指定された時間数分の天気を取得する
     * @param limit 取得する天気の数
     * @return 天気のリスト
     */
    override fun drawWeatherForecast(limit: Int): List<WeatherType> {
        val weatherList = mutableListOf<WeatherType>()
        val now = ZonedDateTime.now()
        val weather = getClimateConfig()
        for (i in 0 until limit) {
            val date = now.plusHours(i * weather.weather.interval.toLong() + weather.weather.offset)
            weatherList.add(getWeatherByDate(date))
        }
        return weatherList
    }

    fun get(x: Long): Long {
        val check = floor(100 / getClimateConfig().weather.maxInclination.toDouble()).toLong()
        if (x % check == 0L) {
            val random = XorShiftRandom(getHash(x)).nextInt(0, 100)
            return random.toLong()
        }
        val small = (XorShiftRandom(getHash(x - (x % check)))).nextInt(0, 100)
        val large = (XorShiftRandom(getHash(x - (x % check) + check))).nextInt(0, 100)
        return ((large - small) / check * (x % check)) + small
    }

    /**
     * 指定された日時における天気を返す
     * @param date 指定された日時
     * @return 天気
     */
    fun getWeatherByDate(date: ZonedDateTime): WeatherType {
        val random = get(getTimeDiff(date))
        val total = getClimateConfig().weather.weight.values.sum()

        val normalizedRandom = (random.toDouble() / 100) * total

        var acc = 0
        for ((weatherType, probability) in getClimateConfig().weather.weight.toList()) {
            acc += probability
            if (normalizedRandom < acc) {
                return weatherType
            }
        }

        return WeatherType.THUNDERSTORM
    }

    /**
     * 指定された日時と開始日時の時間差を計算する
     * @param date 指定された日時
     * @return 時間差（秒）
     */
    private fun getTimeDiff(date: ZonedDateTime): Long {
        val diff = (date.toEpochSecond() - startDate.toEpochSecond()) / 3600 / 8
        return diff
    }

    private fun getHash(x: Long): Long {
        val hashBytes =
            MessageDigest.getInstance(
                "SHA-256",
            ).digest((fishingWorldId.value + getClimateConfig().hashPepper + x.toString()).toByteArray())
        // バイト配列を16進数文字列に変換
        val hexString = hashBytes.joinToString("") { "%02x".format(it) }
        // 16進数文字列の先頭8文字を取得してLongに変換 (TypeScriptの実装に合わせる)
        val value = hexString.substring(0, 8).toLong(16)
        return value
    }
}
