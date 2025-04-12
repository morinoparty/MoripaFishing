package party.morino.moripafishing.random.weather

import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator
import de.articdive.jnoise.pipeline.JNoise
import party.morino.moripafishing.api.model.WeatherType
import party.morino.moripafishing.api.random.weather.WeatherRandomizer
import java.time.ZoneId
import java.time.ZonedDateTime

class WeatherRandomizerImpl : WeatherRandomizer {
    val interval = 8 // 8時間ごとに変化
    val startDate = ZonedDateTime.of(
        2023, 10, 1, 0, 0, 0, 0, ZoneId.of("Asia/Tokyo")
    )
    private var seed: Int = 0

    fun setSeed(seed: Int) {
        this.seed = seed
    }

    override fun getWeather(): WeatherType {
        val now = ZonedDateTime.now()
        return getWeatherByDate(now)
    }

    override fun getFeatureWeather(limit: Int): List<WeatherType> {
        val weatherList = mutableListOf<WeatherType>()
        val now = ZonedDateTime.now()
        for (i in 0 until limit) {
            val date = now.plusHours(i * interval.toLong())
            weatherList.add(getWeatherByDate(date))
        }
        return weatherList
    }

    /*
     * 指定された日時における天気を返す。
     * @param date 指定された日時
     * @return 天気
     */
    fun getWeatherByDate(date: ZonedDateTime): WeatherType {
        return when (getRandomInt(0, 80, date)) {
            in 0..20 -> WeatherType.SUNNY
            in 21..40 -> WeatherType.CLOUDY
            in 41..60 -> WeatherType.RAINY
            else -> WeatherType.THUNDERSTORM
        }
    }

    /*
     * PerlinNoiseを使用して、指定された日時における乱数を生成する。
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
            .scale(0.22)
            .build()

        val x = getTimeDiff(date) / 3600 / interval // 8時間ごとに変化
        val res = noise.evaluateNoise(x.toDouble())
        return ((res + 0.5) * (max - min) + min).toInt().coerceIn(min,max)
    }

    /**
     * 指定された日時と開始日時の時間差を計算する。
     * @param date 指定された日時
     * @return 時間差（秒）
     */
    private fun getTimeDiff(date: ZonedDateTime): Long {
        return date.toEpochSecond() - startDate.toEpochSecond()
    }
}