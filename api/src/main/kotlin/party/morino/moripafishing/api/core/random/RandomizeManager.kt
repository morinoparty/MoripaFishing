package party.morino.moripafishing.api.core.random

import party.morino.moripafishing.api.core.random.fish.FishRandomizer
import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * ランダムな値を生成するためのインターフェース
 */
interface RandomizeManager {
    /**
     * 魚のランダム化を行う。
     * @return 魚のランダム化を行うインスタンス
     */
    fun getFishRandomizer(): FishRandomizer

    /**
     * 天候のランダム化を行う。
     * @return 天候のランダム化を行うインスタンス
     */
    fun getWeatherRandomizer(fishingWorldId: FishingWorldId): WeatherRandomizer
}
