package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 天候に関する設定を保持するデータクラス
 *
 * ワールドごとの天候制御・時間同期の有効/無効を管理する。
 * `WorldDetailConfig.climateConfig` でワールドごとにオーバーライド可能で、
 * 未設定の場合は `WorldConfig.defaultClimateConfig` がフォールバックとして使用される。
 *
 * @property weatherMode 天候の決定ソース。
 *   `INTERNAL` の場合、プラグインが内蔵ランダマイザーで天候を決定し Bukkit ワールドにも適用する
 *   （`DO_WEATHER_CYCLE = false`）。
 *   `VANILLA` の場合、Bukkit のバニラ天候状態を読み取り魚の抽選条件にのみ使用する
 *   （`DO_WEATHER_CYCLE = true`）。
 *   `EXTERNAL` の場合、外部プラグインが登録した `WeatherProvider` に委譲する
 *   （`DO_WEATHER_CYCLE = false`）。
 * @property enableDayCycle プラグインによる時間同期を有効にするかどうか。
 *   `true` の場合、`DO_DAYLIGHT_CYCLE` を `false` に設定しプラグインがリアルタイム同期または固定時間を管理する。
 *   `false` の場合、`DO_DAYLIGHT_CYCLE` を `true` に設定しバニラの昼夜サイクルに委ねる。
 * @property constant 固定の天候・時間設定
 * @property dayCycle 昼夜サイクルの設定
 * @property weather 天候変化の設定
 * @property hashPepper ハッシュ計算用のペッパー値
 */
@Serializable(with = ClimateConfigSerializer::class)
data class ClimateConfig(
    val weatherMode: WeatherMode = WeatherMode.INTERNAL,
    val enableDayCycle: Boolean = true,
    val constant: ConstantConfig = ConstantConfig(),
    val dayCycle: DayCycleConfig = DayCycleConfig(),
    val weather: WeatherConfig = WeatherConfig(),
    val hashPepper: String = "pepper",
)

@Serializable
private data class ClimateConfigSurrogate(
    val weatherMode: WeatherMode = WeatherMode.INTERNAL,
    val enableDayCycle: Boolean = true,
    val constant: ConstantConfig = ConstantConfig(),
    val dayCycle: DayCycleConfig = DayCycleConfig(),
    val weather: WeatherConfig = WeatherConfig(),
    val hashPepper: String = "pepper",
)

/**
 * `enableWeather: Boolean` (旧) と `weatherMode: WeatherMode` (新) の双方を受理するシリアライザー。
 *
 * - `weatherMode` が存在すればそちらを優先
 * - なければ `enableWeather`: `true` → `INTERNAL`, `false` → `VANILLA` にマップ
 * - 書き出し時は常に新形式の `weatherMode` のみ
 */
object ClimateConfigSerializer : KSerializer<ClimateConfig> {
    private val surrogate = ClimateConfigSurrogate.serializer()

    override val descriptor: SerialDescriptor = surrogate.descriptor

    override fun deserialize(decoder: Decoder): ClimateConfig {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            val obj = element as? JsonObject ?: return ClimateConfig()
            val rewritten: JsonElement =
                if (!obj.containsKey("weatherMode") && obj.containsKey("enableWeather")) {
                    val legacy = obj["enableWeather"]?.jsonPrimitive?.boolean ?: true
                    val mode = if (legacy) WeatherMode.INTERNAL else WeatherMode.VANILLA
                    buildJsonObject {
                        for ((k, v) in obj) {
                            if (k == "enableWeather") continue
                            put(k, v)
                        }
                        put("weatherMode", JsonPrimitive(mode.name))
                    }
                } else {
                    obj
                }
            val s = decoder.json.decodeFromJsonElement(surrogate, rewritten)
            return ClimateConfig(
                weatherMode = s.weatherMode,
                enableDayCycle = s.enableDayCycle,
                constant = s.constant,
                dayCycle = s.dayCycle,
                weather = s.weather,
                hashPepper = s.hashPepper,
            )
        }
        val s = surrogate.deserialize(decoder)
        return ClimateConfig(
            weatherMode = s.weatherMode,
            enableDayCycle = s.enableDayCycle,
            constant = s.constant,
            dayCycle = s.dayCycle,
            weather = s.weather,
            hashPepper = s.hashPepper,
        )
    }

    override fun serialize(
        encoder: Encoder,
        value: ClimateConfig,
    ) {
        val s =
            ClimateConfigSurrogate(
                weatherMode = value.weatherMode,
                enableDayCycle = value.enableDayCycle,
                constant = value.constant,
                dayCycle = value.dayCycle,
                weather = value.weather,
                hashPepper = value.hashPepper,
            )
        if (encoder is JsonEncoder) {
            encoder.encodeJsonElement(encoder.json.encodeToJsonElement(surrogate, s))
        } else {
            surrogate.serialize(encoder, s)
        }
    }
}
