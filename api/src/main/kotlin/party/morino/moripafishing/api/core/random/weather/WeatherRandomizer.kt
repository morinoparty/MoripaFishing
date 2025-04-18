package party.morino.moripafishing.api.core.random.weather

import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.api.model.world.FishingWorldId


/**
 * 天気のランダム化を行うインターフェース
 * PerlinNoiseを使用して、時間に基づいた天気の生成を行う
 */
interface WeatherRandomizer {
    /**
     * 現在の天気を抽選する
     * 
     * @param fishingWorldId 釣り場のID
     * @return 抽選された天気
     */
    fun drawWeather(fishingWorldId: FishingWorldId): WeatherType

    /**
     * 指定された時間数分の天気を抽選する
     * 
     * @param limit 抽選する天気の数
     * @param fishingWorldId 釣り場のID
     * @return 抽選された天気のリスト
     */
    fun drawWeatherForecast(limit: Int, fishingWorldId: FishingWorldId): List<WeatherType>

    /**
     * 釣り場のIDに基づいて乱数生成のシード値を設定する
     * 
     * @param fishingWorldId 釣り場のID
     */
    fun setSeedWithWorldId(fishingWorldId: FishingWorldId)
}