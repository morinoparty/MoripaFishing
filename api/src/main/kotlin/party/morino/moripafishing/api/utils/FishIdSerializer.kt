package party.morino.moripafishing.api.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import party.morino.moripafishing.api.model.fish.FishId

/**
 * FishIdのシリアライザー
 */
object FishIdSerializer : KSerializer<FishId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FishId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FishId) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): FishId {
        return FishId(decoder.decodeString())
    }
} 