package party.morino.moripafishing.random.weather

import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator
import de.articdive.jnoise.pipeline.JNoise
import party.morino.moripafishing.api.model.WeatherType
import party.morino.moripafishing.api.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.config.ConfigManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.WeatherConfig
import party.morino.moripafishing.api.config.WorldConfig
import party.morino.moripafishing.api.world.WorldId
import party.morino.moripafishing.api.world.WorldManager
import java.time.ZoneId
import java.time.ZonedDateTime
import java.security.MessageDigest
import kotlin.random.Random

/**
 * 天気をランダムに生成する実装クラス
 * PerlinNoiseを使用して、時間に基づいた天気の生成を行う
 */
class WeatherRandomizerImpl : WeatherRandomizer, KoinComponent {
    private val configManager : ConfigManager by inject()
    private val worldManager : WorldManager by inject()

    private fun getWeatherConfig(worldId: WorldId): WeatherConfig =
         worldManager.getWorldDetailConfig(worldId)?.weatherConfig  ?: configManager.getConfig().defaultWeatherConfig

    private val startDate = ZonedDateTime.of(
        2023, 10, 1, 0, 0, 0, 0, ZoneId.of(configManager.getConfig().defaultWeatherConfig.dayCycleTimeZone)
    )

    private var seed : Int = 0

    init{
        setSeed(0)
    }

    /**
     * 乱数生成のシード値を設定する
     * @param seed 設定するシード値
     */
    fun setSeed(seed: Int) {
        //papperを付けて、seedを生成し予測不能にする
        val papper = configManager.getConfig().defaultWeatherConfig.hashPepper
        val hashed = MessageDigest.getInstance("SHA-256").digest((papper + seed).toByteArray()).let {
            // Longに変換してからIntの範囲に収める
            (it.take(3).joinToString("") { "%02x".format(it) }.toLong(16) and 0x7FFFFFFF).toInt()
        }
        this.seed = hashed
    }

    /**
     *
     */
    fun setSeedWithWorldId(worldId: WorldId) {
        val papper = configManager.getConfig().defaultWeatherConfig.hashPepper
        val hashed = MessageDigest.getInstance("SHA-256").digest((papper + worldId).toByteArray()).let {
            (it.take(3).joinToString("") { "%02x".format(it) }.toLong(16) and 0x7FFFFFFF).toInt()
        }
        this.seed = hashed
    }

    /**
     * 現在の天気を取得する
     * @return 現在の天気
     */
    override fun getWeather(worldId: WorldId): WeatherType {
        val now = ZonedDateTime.now()
        return getWeatherByDate(now,worldId)
    }

    /**
     * 指定された時間数分の天気を取得する
     * @param limit 取得する天気の数
     * @return 天気のリスト
     */
    override fun getFeatureWeather(limit: Int, worldId: WorldId): List<WeatherType> {
        val weatherList = mutableListOf<WeatherType>()
        val now = ZonedDateTime.now()
        val weather = getWeatherConfig(worldId)
        for (i in 0 until limit) {
            val date = now.plusHours(i * weather.interval.toLong() + weather.offset)
            weatherList.add(getWeatherByDate(date,worldId))
        }
        return weatherList
    }

    /**
     * 指定された日時における天気を返す
     * @param date 指定された日時
     * @return 天気
     */
    fun getWeatherByDate(date: ZonedDateTime, worldId: WorldId): WeatherType {
        val weatherConfig = getWeatherConfig(worldId)
        val weatherSetting = weatherConfig.weatherSetting
        val total = weatherSetting.values.sum()
        val totalList = weatherSetting.toList()
        val random = getRandomInt(0, total, date, worldId)

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
    fun getRandomInt(min: Int, max: Int, date: ZonedDateTime, worldId: WorldId): Int {
        val noise = JNoise.newBuilder()
            .perlin(PerlinNoiseGenerator
                .newBuilder()
                .setSeed(seed.toLong())
                .build())
            .clamp(-0.5 , 0.5)
            .scale(getWeatherConfig(worldId).frequency)
            .build()

        val x = getTimeDiff(date) / 3600 / getWeatherConfig(worldId).interval
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