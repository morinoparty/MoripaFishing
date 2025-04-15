package party.morino.moripafishing.api.core.world

import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData

/**
 * FishingWorldインターフェースは、釣りワールドに関する情報を提供する。
 */
interface FishingWorld {
    
    /**
     * ワールドのIDを取得する。
     * @return FishingWorldId ワールドのID
     */
    fun getId() : FishingWorldId

    /**
     * 計算された天候タイプを取得する。
     * @return WeatherType 計算された天候タイプ
     */
    fun getCalculatedWeather(): WeatherType

    /**
     * 現在の天候タイプを取得する。
     * @return WeatherType 現在の天候タイプ
     */
    fun getCurrentWeather(): WeatherType

    /**
     * 天候タイプを設定する。
     * @param weatherType 天候タイプ
     */
    fun setWeather(weatherType: WeatherType)

    /**
     * 天候タイプを更新する
     */
    fun updateWeather() = setWeather(getCalculatedWeather())

    /**
     * ワールドのスポーン位置を取得する。
     * @return LocationData ワールドのスポーン位置
     */
    fun getWorldSpawnPosition() : LocationData

    /**
     * ワールドの半径を取得する。
     * @return Int ワールドの半径
     */
    fun getRadius(): Int

    /**
     * ワールドの中心位置を取得する。
     * @return LocationData ワールドの中心位置
     */
    fun getCenter(): LocationData

}