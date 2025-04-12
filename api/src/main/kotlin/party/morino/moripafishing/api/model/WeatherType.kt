package party.morino.moripafishing.api.model

enum class WeatherType(name : String) {
    SUNNY("晴れ"),
    CLOUDY("曇り"),
    RAINY("雨"),
    THUNDERSTORM("雷"),
    SNOWY("雪"),
    FOGGY("霧");

    override fun toString(): String {
        return name
    }
}