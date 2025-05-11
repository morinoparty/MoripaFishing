import kotlinx.serialization.json.Json

object Utils {
    val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            prettyPrint = true
        }
}
