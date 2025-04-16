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
     * ワールドの大きさを取得する。
     * @return Double ワールドの大きさ
     * */
    fun getSize(): Double


    /**
     * ワールドの大きさを設定する。
     * @param size ワールドの大きさ
     */
    fun setSize(size : Double)

    /**
     * ワールドの中心位置を取得する。
     * @return LocationData ワールドの中心位置
     */
    fun getCenter(): LocationData

    /**
     * ワールドの中心位置を取得する。
     * @param locationData ワールドの中心位置
     */
    fun setCenter(locationData: LocationData)

    /**
     * ワールドの設定を更新する。
     * ワールドの設定は、ワールドの中心位置や半径、天気および時間などの情報を含む。
     * このメソッドは、ワールドの設定を更新するために使用される。
     */
    fun refreshSetting()
}