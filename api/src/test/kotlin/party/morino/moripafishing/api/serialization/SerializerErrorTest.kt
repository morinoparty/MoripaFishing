package party.morino.moripafishing.api.serialization

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * 不正な値が `SerializationException` (`IllegalArgumentException`) として報告されることを確認する。
 */
class SerializerErrorTest {
    @Test
    fun `a valid key round-trips`() {
        val key = Key.key("moripafishing", "weather")
        assertEquals(key, Json.decodeFromString(KeySerializer, Json.encodeToString(KeySerializer, key)))
    }

    @Test
    fun `an invalid key is a SerializationException`() {
        assertThrows<SerializationException> { Json.decodeFromString(KeySerializer, "\"Bad Key\"") }
    }

    @Test
    fun `an invalid date-time is a SerializationException`() {
        assertThrows<SerializationException> { Json.decodeFromString(ZonedDateTimeSerializer, "\"yesterday\"") }
    }
}
