package party.morino.moripafishing.api.utils.serializer

import kotlinx.serialization.Serializable
import java.util.UUID

// UUIDをシリアライズ・デシリアライズするためのカスタムシリアライザ
@Serializable
object UUIDSerializer : kotlinx.serialization.KSerializer<UUID> {
    override val descriptor =
        kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
            "UUID",
            kotlinx.serialization.descriptors.PrimitiveKind.STRING,
        )

    override fun serialize(
        encoder: kotlinx.serialization.encoding.Encoder,
        value: UUID,
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): UUID = UUID.fromString(decoder.decodeString())
}
