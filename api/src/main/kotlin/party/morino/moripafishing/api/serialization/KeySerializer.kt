package party.morino.moripafishing.api.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

/**
 * Adventure の [Key] を `namespace:value` 文字列として直列化するシリアライザー。
 *
 * 不正なキーは他の不正な値と同じく `SerializationException` として扱う。
 */
object KeySerializer : KSerializer<Key> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Key", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Key,
    ) {
        encoder.encodeString(value.asString())
    }

    override fun deserialize(decoder: Decoder): Key {
        val value = decoder.decodeString()
        return try {
            Key.key(value)
        } catch (e: InvalidKeyException) {
            throw SerializationException("Invalid key '$value': ${e.message}", e)
        }
    }
}
