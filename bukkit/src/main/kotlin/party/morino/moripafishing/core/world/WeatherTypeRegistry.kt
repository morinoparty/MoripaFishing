package party.morino.moripafishing.core.world

import party.morino.moripafishing.api.core.world.WeatherEffect
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.world.effect.*

/**
 * 天候タイプごとにWeatherEffectを返すレジストリ
 */
object WeatherTypeRegistry {
    fun getEffect(type: WeatherType): WeatherEffect {
        return when (type) {
            WeatherType.SUNNY -> SunnyWeatherEffect()
            WeatherType.RAINY -> RainWeatherEffect()
            WeatherType.THUNDERSTORM -> ThunderWeatherEffect()
            WeatherType.CLOUDY -> CloudyWeatherEffect()
            // 未対応の天候はとりあえず晴れにフォールバック
            else -> SunnyWeatherEffect()
        }
    }
}
