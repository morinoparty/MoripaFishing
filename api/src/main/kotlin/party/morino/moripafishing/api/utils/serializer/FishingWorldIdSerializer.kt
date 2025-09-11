package party.morino.moripafishing.api.utils.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * FishingWorldIdのシリアライザー
 */
object FishingWorldIdSerializer : KSerializer<FishingWorldId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FishingWorldId", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: FishingWorldId,
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): FishingWorldId = FishingWorldId(decoder.decodeString())
}
