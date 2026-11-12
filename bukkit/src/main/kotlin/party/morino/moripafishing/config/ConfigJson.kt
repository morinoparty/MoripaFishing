package party.morino.moripafishing.config

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * コアの設定・定義ファイル (config・world・fish・rarity) の読み書きに使う JSON 設定。未知のキーはエラーにする。
 */
internal val ConfigJson: Json =
    Json {
        prettyPrint = true
        encodeDefaults = true
    }

/** JSON Schema の参照に使うキー。読み込み時に無視する。 */
private const val SCHEMA_KEY = "\$schema"

/**
 * トップレベルの `$schema` キーを取り除いてからデコードする。
 *
 * @throws IllegalArgumentException JSON が不正な場合 (`SerializationException` を含む)
 */
internal fun <T> Json.decodeConfig(
    deserializer: DeserializationStrategy<T>,
    text: String,
): T {
    val element = parseToJsonElement(text)
    val stripped = if (element is JsonObject && SCHEMA_KEY in element) JsonObject(element - SCHEMA_KEY) else element
    return decodeFromJsonElement(deserializer, stripped)
}
