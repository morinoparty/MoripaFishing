package party.morino.moripafishing.event.world

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 釣りワールドの天候 (`FishingWorld.getWeather`) が変化した時に発生します。
 *
 * コアのリフレッシュループがメインスレッドで発火します。変化はリフレッシュ間隔
 * (`WorldConfig.refreshIntervalSeconds`) の粒度で検知され、初回の観測では発火しません。
 * キャンセルはできません。
 *
 * @param worldId 天候が変化した釣りワールドのID
 * @param oldWeather 変化前の天候
 * @param newWeather 変化後の天候
 */
class FishingWorldWeatherChangeEvent(
    private val worldId: FishingWorldId,
    private val oldWeather: WeatherType,
    private val newWeather: WeatherType,
) : Event() {
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
