package party.morino.moripafishing.core.random.weather

import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator
import de.articdive.jnoise.pipeline.JNoise
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.WeatherConfig
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.core.world.WorldManager
import java.security.MessageDigest
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * 天気をランダムに生成する実装クラス
 * PerlinNoiseを使用して、時間に基づいた天気の生成を行う
 */
class WeatherRandomizerImpl : WeatherRandomizer, KoinComponent {
    private val configManager : ConfigManager by inject()
    private val worldManager : WorldManager by inject()

    private fun getWeatherConfig(fishingWorldId: FishingWorldId): WeatherConfig {
        return worldManager.getWorldDetails(fishingWorldId)?.weatherConfig
            ?: configManager.getConfig().defaultWeatherConfig
    }

    private val startDate : ZonedDateTime by lazy{
        ZonedDateTime.of(
            2023, 10, 1, 0, 0, 0, 0, ZoneId.of(configManager.getConfig().defaultWeatherConfig.dayCycleTimeZone)
        )
    }

    private var seed : Int = 0

    /**
     * 乱数生成のシード値を設定する
     * @param seed 設定するシード値
     */
    fun setSeed(seed: Int) {
        val pepper = configManager.getConfig().defaultWeatherConfig.hashPepper
        val hashed = MessageDigest.getInstance("SHA-256").digest((pepper + seed).toByteArray()).let {
            // Longに変換してからIntの範囲に収める
            (it.take(3).joinToString("") { "%02x".format(it) }.toLong(16) and 0x7FFFFFFF).toInt()
        }
        this.seed = hashed
    }

    /**
     *
     */
    override fun setSeedWithWorldId(fishingWorldId: FishingWorldId) {
        val pepper = configManager.getConfig().defaultWeatherConfig.hashPepper
        val hashed = MessageDigest.getInstance("SHA-256").digest((pepper + fishingWorldId).toByteArray()).let {
            (it.take(3).joinToString("") { "%02x".format(it) }.toLong(16) and 0x7FFFFFFF).toInt()
        }
        this.seed = hashed
    }

    /**
     * 現在の天気を取得する
     * @return 現在の天気
     */
    override fun getWeather(fishingWorldId: FishingWorldId): WeatherType {
        val now = ZonedDateTime.now()
        return getWeatherByDate(now, fishingWorldId)
    }

    /**
     * 指定された時間数分の天気を取得する
     * @param limit 取得する天気の数
     * @return 天気のリスト
     */
    override fun getFeatureWeather(limit: Int, fishingWorldId: FishingWorldId): List<WeatherType> {
        val weatherList = mutableListOf<WeatherType>()
        val now = ZonedDateTime.now()
        val weather = getWeatherConfig(fishingWorldId)
        for (i in 0 until limit) {
            val date = now.plusHours(i * weather.interval.toLong() + weather.offset)
            weatherList.add(getWeatherByDate(date, fishingWorldId))
        }
        return weatherList
    }

    /**
     * 指定された日時における天気を返す
     * @param date 指定された日時
     * @return 天気
     */
    fun getWeatherByDate(date: ZonedDateTime, fishingWorldId: FishingWorldId): WeatherType {
        val weatherConfig = getWeatherConfig(fishingWorldId)
        val weatherSetting = weatherConfig.weatherSetting
        val total = weatherSetting.values.sum()
        val totalList = weatherSetting.toList()
        val random = getRandomInt(0, total, date, fishingWorldId)

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
    fun getRandomInt(min: Int, max: Int, date: ZonedDateTime, fishingWorldId: FishingWorldId): Int {
        val noise = JNoise.newBuilder()
            .perlin(PerlinNoiseGenerator
                .newBuilder()
                .setSeed(seed.toLong())
                .build())
            .clamp(-0.5 , 0.5)
            .scale(getWeatherConfig(fishingWorldId).frequency)
            .build()

        val x = getTimeDiff(date) / 3600 / getWeatherConfig(fishingWorldId).interval
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