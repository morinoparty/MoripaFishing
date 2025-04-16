package party.morino.moripafishing.core.random.weather

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import java.time.ZonedDateTime
import kotlin.math.absoluteValue

@ExtendWith(MoripaFishingTest::class)
class WeatherRandomizerImplTest: KoinTest {
    private val worldManager : WorldManager by inject()
    private val weatherRandomizer by lazy {
        WeatherRandomizerImpl()
    }

    @Test
    @DisplayName("Get weather for fishing world")
    fun test(){
        weatherRandomizer.setSeed(1234)
    }

    /**
     * ランダムな天気を取得するテスト
     * 1000回の天気を取得し、隣接する天気の順序が1以内であることを確認する
     * 実行するには、以下のコマンドを実行してください。
     * ```
     * ./gradlew test --tests "party.morino.moripafishing.random.weather.WeatherRandomizerImplTest.getRandomWeather"
     * ```
     */
    @Test
    @DisplayName("ランダムな天気を取得するテスト")
    fun getRandomWeather() {
        repeat(10) {
            weatherRandomizer.setSeed(it)
            val weatherList = weatherRandomizer.getFeatureWeather(10000, worldManager.getDefaultWorldId())
            val order = weatherList.map { it.ordinal }
            val diff = order.zipWithNext { a, b -> (b - a).absoluteValue }
            // println("max: ${diff.maxOrNull()} min: ${diff.minOrNull()}")
             val rate = weatherList.groupingBy { it }.eachCount().toList().sortedByDescending { (_, v) -> v }.map { (k, v) -> "$k : ${v.toDouble() / weatherList.size}" }
             println(rate)
            assert(diff.max() <= 1)
        }
    }

    /**
     * 天気の差分範囲を取得するテスト
     * 100回のランダムな整数を取得し、その最大値と最小値を表示する
     */
    @Test
    @DisplayName("天気の差分範囲を取得するテスト")
    fun getDiffRange() {
        val times = (1..100).map {
            ZonedDateTime.now().plusHours(it * 8.toLong())
        }
        val res = times.map {
            weatherRandomizer.getRandomInt(0, 100, it, FishingWorldId("default"))
        }
        println(res)
        val diff = res.zipWithNext { a, b -> b - a }
        println("max: ${diff.maxOrNull()} min: ${diff.minOrNull()}")
    }

}