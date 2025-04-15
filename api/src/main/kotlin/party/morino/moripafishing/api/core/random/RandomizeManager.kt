package party.morino.moripafishing.api.core.random

import party.morino.moripafishing.api.core.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.core.random.fish.FishRandomizer

/**
 * ランダムな値を生成するためのインターフェース
 */
interface RandomizeManager {
    /**
     * 天気のランダム化を行う。
     * @return 天気のランダム化を行うインスタンス
     */
    fun getWeatherRandomizer(): WeatherRandomizer

    /**
     * 魚のランダム化を行う。
     * @return 魚のランダム化を行うインスタンス
     */
    fun getFishRandomizer(): FishRandomizer
}