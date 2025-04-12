package party.morino.moripafishing.random.weather

import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator
import de.articdive.jnoise.pipeline.JNoise
import party.morino.moripafishing.api.model.WeatherType
import party.morino.moripafishing.api.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.config.ConfigManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * 天気をランダムに生成する実装クラス
 * PerlinNoiseを使用して、時間に基づいた天気の生成を行う
 */
class WeatherRandomizerImpl : WeatherRandomizer, KoinComponent {
    private val configManager : ConfigManager by inject()

    private val startDate = ZonedDateTime.of(
        2023, 10, 1, 0, 0, 0, 0, ZoneId.of(configManager.getConfig().weather.dayCycleTimeZone)
    )
    private var seed: Int = 0

    /**
     * 乱数生成のシード値を設定する
     * @param seed 設定するシード値
     */
    fun setSeed(seed: Int) {
        this.seed = seed
    }

    /**
     * 現在の天気を取得する
     * @return 現在の天気
     */
    override fun getWeather(): WeatherType {
        val now = ZonedDateTime.now()
        return getWeatherByDate(now)
    }

    /**
     * 指定された時間数分の天気を取得する
     * @param limit 取得する天気の数
     * @return 天気のリスト
     */
    override fun getFeatureWeather(limit: Int): List<WeatherType> {
        val weatherList = mutableListOf<WeatherType>()
        val now = ZonedDateTime.now()
        val weatherConfig = configManager.getConfig().weather
        for (i in 0 until limit) {
            val date = now.plusHours(i * weatherConfig.interval.toLong() + weatherConfig.offset.toLong())
            weatherList.add(getWeatherByDate(date))
        }
        return weatherList
    }

    /**
     * 指定された日時における天気を返す
     * @param date 指定された日時
     * @return 天気
     */
    fun getWeatherByDate(date: ZonedDateTime): WeatherType {
        val weatherConfig = configManager.getConfig().weather
        val weatherSetting = weatherConfig.weatherSetting
        val total = weatherSetting.values.sum()
        val totalList = weatherSetting.toList()
        val random = getRandomInt(0, total, date)

        var sum = 0
        for (i in totalList.indices) {
            sum += totalList[i].second
            if (random in 0..sum) {
                return totalList[i].first
            }
        }
        return WeatherType.THUNDERSTORM
    }

    /**
     * PerlinNoiseを使用して、指定された日時における乱数を生成する
     * @param min 最小値
     * @param max 最大値
     * @param date 指定された日時
     * @return 乱数
     */
    fun getRandomInt(min: Int, max: Int, date: ZonedDateTime): Int {
        val noise = JNoise.newBuilder()
            .perlin(PerlinNoiseGenerator
                .newBuilder()
                .setSeed(seed.toLong())
                .build())
            .clamp(-0.5 , 0.5)
            .scale(configManager.getConfig().weather.frequency)
            .build()

        val x = getTimeDiff(date) / 3600 / configManager.getConfig().weather.interval
        val res = noise.evaluateNoise(x.toDouble())
        return ((res + 0.5) * (max - min) + min).toInt().coerceIn(min,max)
    }

    /**
     * 指定された日時と開始日時の時間差を計算する
     * @param date 指定された日時
     * @return 時間差（秒）
     */
    private fun getTimeDiff(date: ZonedDateTime): Long {
        return date.toEpochSecond() - startDate.toEpochSecond()
    }
}