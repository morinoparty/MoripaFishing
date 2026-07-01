package party.morino.moripafishing.api.utils.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.key.Key

/**
 * Adventure の [Key] を `namespace:value` 文字列として直列化するシリアライザー。
 */
object KeySerializer : KSerializer<Key> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Key", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Key,
    ) {
        encoder.encodeString(value.asString())
    }

    override fun deserialize(decoder: Decoder): Key = Key.key(decoder.decodeString())
}
