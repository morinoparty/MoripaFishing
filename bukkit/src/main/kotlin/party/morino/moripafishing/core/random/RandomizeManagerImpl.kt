package party.morino.moripafishing.core.random

import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.fish.FishRandomizer
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.core.random.fish.FishRandomizerImpl
import party.morino.moripafishing.core.random.weather.WeatherRandomizerImpl

/**
 * RandomizeManagerの実装クラス
 */
class RandomizeManagerImpl : RandomizeManager {
    /**
     * 魚のランダム化を行うインスタンスを返す
     * @return 魚のランダム化を行うインスタンス
     */
    override fun getFishRandomizer(): FishRandomizer {
        return FishRandomizerImpl()
    }

    /**
     * 魚のランダム化を行うインスタンスを返す
     * @param fishingWorldId フィッシングワールドのID
     * @return 魚のランダム化を行うインスタンス
     */
    override fun getWeatherRandomizer(fishingWorldId: FishingWorldId): WeatherRandomizer {
        return WeatherRandomizerImpl(fishingWorldId)
    }
}
