package party.morino.moripafishing.api.core.random.weather

import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.api.model.world.FishingWorldId


/**
 * 天気のランダム化を行うインターフェース
 */
interface WeatherRandomizer {
    /**
     * 天気をランダムに取得する。
     * @return 天気
     */
    fun getWeather(fishingWorldId: FishingWorldId): WeatherType

    /**
     * 指定された回数分の天気をランダムに取得する。
     * @param limit 天気の回数
     * @return 天気のリスト
     */
    fun getFeatureWeather(limit: Int, fishingWorldId: FishingWorldId): List<WeatherType>


    fun setSeedWithWorldId(fishingWorldId: FishingWorldId)
}