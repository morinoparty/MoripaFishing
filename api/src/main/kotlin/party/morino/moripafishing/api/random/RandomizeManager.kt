package party.morino.moripafishing.api.random

import party.morino.moripafishing.api.random.weather.WeatherRandomizer
import party.morino.moripafishing.api.random.fish.FishRandomizer

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