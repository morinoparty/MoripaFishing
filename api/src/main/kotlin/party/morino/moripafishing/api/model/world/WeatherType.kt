package party.morino.moripafishing.api.model.world

/**
 * 天候の種類を表す列挙型
 */
enum class WeatherType(
    name: String,
) {
    /**
     * 晴れの天候
     */
    SUNNY("晴れ"),

    /**
     * 曇りの天候
     */
    CLOUDY("曇り"),

    /**
     * 雨の天候
     */
    RAINY("雨"),

    /**
     * 雷の天候
     */
    THUNDERSTORM("雷"),

    /**
     * 雪の天候
     */
    SNOWY("雪"),

    /**
     * 霧の天候
     */
    FOGGY("霧"),
    ;

    /**
     * 天候の名前を文字列として返す
     * @return 天候の名前
     */
    override fun toString(): String = name
}
