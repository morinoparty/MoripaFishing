package party.morino.moripafishing.random

import party.morino.moripafishing.api.random.RandomizeManager
import party.morino.moripafishing.api.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.random.fish.FishRandomizer
import party.morino.moripafishing.random.weather.WeatherRandomizerImpl
import party.morino.moripafishing.random.fish.FishRandomizerImpl
import java.util.Random

/**
 * RandomizeManagerの実装クラス
 */
class RandomizeManagerImpl : RandomizeManager {
    private val random = Random()

    /**
     * 天気をランダムに生成する実装クラスを返す
     * @return 天気をランダムに生成する実装クラス
     */
    override fun getWeatherRandomizer(): WeatherRandomizer {
        return WeatherRandomizerImpl()
    }

    /**
     * 魚をランダムに生成する実装クラスを返す
     * @return 魚をランダムに生成する実装クラス
     */
    override fun getFishRandomizer(): FishRandomizer {
        return FishRandomizerImpl()
    }
} 