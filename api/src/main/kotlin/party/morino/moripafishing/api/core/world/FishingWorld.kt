package party.morino.moripafishing.api.core.world

import party.morino.moripafishing.api.config.world.WorldDetailConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * FishingWorldインターフェースは、釣りワールドに関する情報を提供する。
 */
interface FishingWorld {
    /**
     * ワールドのIDを取得する。
     * @return FishingWorldId ワールドのID
     */
    fun getId(): FishingWorldId

    /**
     * 計算された天候タイプを取得する。
     * @return WeatherType 計算された天候タイプ
     */
    fun getCalculatedWeather(): WeatherType

    /**
     * 現在の天候タイプを取得する。
     * 魚の計算などの場合は、プレイヤーの認識を阻害しないためにこちらを使用する。
     * @return WeatherType 現在の天候タイプ
     */
    fun getCurrentWeather(): WeatherType

    /**
     * 天候タイプを設定する。
     * @param weatherType 天候タイプ
     */
    fun setWeather(weatherType: WeatherType)

    /**
     * 現在の天候ソースが決定した天候（[getCurrentWeather]）をワールドへ適用する。
     * `managesWorldWeather = true` なソースのみ効果があり、内蔵ランダマイザーだけでなく
     * 外部の managed ソースもプロバイダーの決定がそのまま適用される。
     */
    fun updateWeather() = setWeather(getCurrentWeather())

    /**
     * ワールドのスポーン位置を取得する。
     * @return LocationData ワールドのスポーン位置
     */
    fun getWorldSpawnPosition(): LocationData

    /**
     * ワールドのスポーン位置を設定する。
     * @param location ワールドのスポーン位置
     */
    fun setWorldSpawnPosition(location: LocationData)

    /**
     * ワールドボーダーの大きさを取得する。
     * @return Double ワールドボーダーの大きさ
     * */
    fun getSize(): Double

    /**
     * ワールドボーダーの大きさを設定する。
     * @param size ワールドボーダーの大きさ
     */
    fun setSize(size: Double)

    /**
     * ワールドの中心位置 (x, z) を取得する。
     * @return ワールドの中心位置 (x, z)
     */
    fun getCenter(): Pair<Double, Double>

    /**
     * ワールドの中心位置 (x, z) を設定する。
     * @param center ワールドの中心位置 (x, z)
     */
    fun setCenter(center: Pair<Double, Double>)

    /**
     * ワールドの時間を現実時間と同期する。
     */
    fun synchronizeTime()

    /**
     * ワールドの詳細設定を取得する。
     * @return WorldDetailConfig ワールドの詳細設定
     */
    fun getWorldDetails(): WorldDetailConfig

    /**
     * ワールド上で天候や特殊効果などの一連の効果が終了した際に呼び出すメソッド
     * 例: 天候イベントの終了処理や、バフ・デバフの解除など
     */
    fun effectFinish()
}
