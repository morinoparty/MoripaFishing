package party.morino.moripafishing.random.weather

import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.math.absoluteValue

class WeatherRandomizerImplTest {
    val weatherRandomizer = WeatherRandomizerImpl()


    @Test
    fun getRandomWeather() {
        val weatherList = weatherRandomizer.getFeatureWeather(1000)
        val order = weatherList.map { it.ordinal }
        println(weatherList.map { "$it : ${it.ordinal}" })
        val diff = order.zipWithNext { a, b -> (b - a).absoluteValue }
        assert(diff.max() <= 1)
    }
//    @Test
//    fun getRandomInt() {
//        val times = (1..100).map {
//            ZonedDateTime.now().plusHours(it * 8.toLong())
//        }
//        val res = times.map {
//            weatherRandomizer.getRandomInt(0, 100, it)
//        }
//        println(res)
//    }

    @Test
    fun getDiffRange() {
        val times = (1..100).map {
            ZonedDateTime.now().plusHours(it * 8.toLong())
        }
        val res = times.map {
            weatherRandomizer.getRandomInt(0, 100, it)
        }
        println(res)
        val diff = res.zipWithNext { a, b -> b - a }
        println("max: ${diff.maxOrNull()} min: ${diff.minOrNull()}")
    }


}