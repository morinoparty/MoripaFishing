package party.morino.moripafishing.event.world

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * このイベントは、釣りワールドの実効天候が変化した後に発生します。
 *
 * 内蔵ランダマイザーによる定期更新・`FishingWorld.setWeather` の呼び出し・
 * 外部天候ソースの変化検知のいずれでも発火します。
 * 変更適用後に発火するポストイベントであり、キャンセルはできません。
 *
 * 天候の更新は非同期リフレッシュループからも行われるため、
 * 非同期スレッドから発火される場合があります（その場合 `isAsynchronous` が `true` になります）。
 *
 * @param worldId 天候が変化した釣りワールドのID
 * @param oldWeather 変化前の天候
 * @param newWeather 変化後の天候
 * @param async 非同期スレッドから発火する場合は `true`
 */
class FishingWorldWeatherChangeEvent(
    private val worldId: FishingWorldId,
    private val oldWeather: WeatherType,
    private val newWeather: WeatherType,
    async: Boolean,
) : Event(async) {
    companion object {
        @JvmStatic
        private val HANDLER_LIST: HandlerList = HandlerList()

        /**
         * イベントのハンドラリストを取得します。
         * 必須メソッドです。
         * @return イベントのハンドラリスト
         */
        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    fun getWorldId(): FishingWorldId = worldId

    fun getOldWeather(): WeatherType = oldWeather

    fun getNewWeather(): WeatherType = newWeather

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
