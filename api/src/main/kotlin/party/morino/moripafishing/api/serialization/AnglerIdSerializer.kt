package party.morino.moripafishing.api.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import party.morino.moripafishing.api.model.angler.AnglerId
import java.util.UUID

/**
 * AnglerIdのシリアライザー。UUID 文字列として直列化する。
 */
object AnglerIdSerializer : KSerializer<AnglerId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AnglerId", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: AnglerId,
    ) {
        encoder.encodeString(value.value.toString())
    }

    override fun deserialize(decoder: Decoder): AnglerId = AnglerId(UUID.fromString(decoder.decodeString()))
}
