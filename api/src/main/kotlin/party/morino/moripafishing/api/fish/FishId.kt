package party.morino.moripafishing.api.fish

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 魚のIDを表すクラス
 * @property value 魚の一意の識別子
 */
@Serializable(with = FishIdSerializer::class)
class FishId(val value: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FishId) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "FishId(value='$value')"
    }
}

object FishIdSerializer : KSerializer<FishId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FishId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FishId) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): FishId {
        return FishId(decoder.decodeString())
    }
}