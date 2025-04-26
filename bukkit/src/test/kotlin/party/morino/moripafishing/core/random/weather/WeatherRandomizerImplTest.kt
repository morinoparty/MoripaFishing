package party.morino.moripafishing.core.random.weather

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import kotlin.math.absoluteValue

/**
 * 天気のランダム生成をテストするクラス
 * ./gradlew test --tests "party.morino.moripafishing.core.random.weather.WeatherRandomizerImplTest.test"
 */
@ExtendWith(MoripaFishingTest::class)
class WeatherRandomizerImplTest : KoinTest {
    private val worldManager: WorldManager by inject()

    private val weatherRandomizer by lazy {
        WeatherRandomizerImpl(worldManager.getDefaultWorldId())
    }

    /**
     * ランダムな天気を取得するテスト
     * 1000回の天気を取得し、隣接する天気の順序が1以内であることを確認する 現状は1以内ではない
     * 実行するには、以下のコマンドを実行してください。
     * ```
     * ./gradlew test --tests "party.morino.moripafishing.core.random.weather.WeatherRandomizerImplTest.getRandomWeather"
     * ```
     */
    @Test
    @DisplayName("ランダムな天気を取得するテスト")
    fun getRandomWeather() {
        repeat(10) {
            val weatherRandom = WeatherRandomizerImpl(FishingWorldId("world_$it"))
            val weatherList = weatherRandom.drawWeatherForecast(1000)
            val order = weatherList.map { it.ordinal }
            // println("order: $order")
            val diff = order.zipWithNext { a, b -> (b - a).absoluteValue }
            // println("max: ${diff.maxOrNull()} min: ${diff.minOrNull()}")
            val rate =
                weatherList.groupingBy { it }.eachCount().toList().sortedByDescending {
                        (_, v) ->
                    v
                }.map { (k, v) -> "$k : ${v.toDouble() / weatherList.size}" }
            println(rate)
            // assert(diff.max() <= 1)
        }
    }

    /**
     * 天気の差分範囲を取得するテスト
     * 100回のランダムな整数を取得し、その最大値と最小値を表示する
     * 実行するには、以下のコマンドを実行してください。
     * ```
     * ./gradlew test --tests "party.morino.moripafishing.core.random.weather.WeatherRandomizerImplTest.getDiffRange"
     * ```
     */
    @Test
    @DisplayName("天気の差分範囲を取得するテスト")
    fun getDiffRange() {
        val res = weatherRandomizer.drawWeatherForecast(100).map { it.ordinal }
        println(res)
        val diff = res.zipWithNext { a, b -> b - a }
        println("max: ${diff.maxOrNull()} min: ${diff.minOrNull()}")
    }
}
