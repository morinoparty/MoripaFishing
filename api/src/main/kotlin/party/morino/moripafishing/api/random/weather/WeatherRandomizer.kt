package party.morino.moripafishing.api.random.weather

import party.morino.moripafishing.api.model.WeatherType
import party.morino.moripafishing.api.world.WorldId


/**
 * 天気のランダム化を行うインターフェース
 */
interface WeatherRandomizer {
    /**
     * 天気をランダムに取得する。
     * @return 天気
     */
    fun getWeather(worldId: WorldId): WeatherType

    /**
     * 指定された回数分の天気をランダムに取得する。
     * @param limit 天気の回数
     * @return 天気のリスト
     */
    fun getFeatureWeather(limit: Int, worldId: WorldId): List<WeatherType>
}