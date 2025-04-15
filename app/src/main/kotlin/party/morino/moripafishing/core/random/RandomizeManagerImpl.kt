package party.morino.moripafishing.core.random

import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.random.fish.FishRandomizer
import party.morino.moripafishing.core.random.weather.WeatherRandomizerImpl
import party.morino.moripafishing.core.random.fish.FishRandomizerImpl
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