package party.morino.moripafishing.api.world

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * ワールドのIDを表すクラス
 * @param value ワールドのID
 */
@Serializable(with = WorldIdSerializer::class)
class WorldId(val value: String) {
    override fun toString(): String = value
}

object WorldIdSerializer : KSerializer<WorldId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("WorldId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: WorldId) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): WorldId {
        return WorldId(decoder.decodeString())
    }
}