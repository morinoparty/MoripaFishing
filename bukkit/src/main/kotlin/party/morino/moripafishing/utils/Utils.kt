package party.morino.moripafishing.utils
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import party.morino.moripafishing.api.serialization.RegexSerializer

object Utils {
    val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            prettyPrint = true
            allowStructuredMapKeys = true // Regexキーを持つMapのシリアライゼーションを有効化
            serializersModule =
                SerializersModule {
                    contextual(Regex::class, RegexSerializer) // Regexシリアライザーを登録
                }
        }
}
