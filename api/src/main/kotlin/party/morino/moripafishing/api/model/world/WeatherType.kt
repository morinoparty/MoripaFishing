package party.morino.moripafishing.api.model.world

/**
 * 天候の種類を表す列挙型
 */
enum class WeatherType {
    /**
     * 晴れの天候
     */
    SUNNY,

    /**
     * 曇りの天候
     */
    CLOUDY,

    /**
     * 雨の天候
     */
    RAINY,

    /**
     * 雷の天候
     */
    THUNDERSTORM,

    /**
     * 雪の天候
     */
    SNOWY,

    /**
     * 霧の天候
     */
    FOGGY,
}
