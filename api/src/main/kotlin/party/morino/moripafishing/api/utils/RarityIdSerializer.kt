package party.morino.moripafishing.api.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import party.morino.moripafishing.api.model.rarity.RarityId

/**
 * RarityIdのシリアライザー
 */
object RarityIdSerializer : KSerializer<RarityId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RarityId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: RarityId) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): RarityId {
        return RarityId(decoder.decodeString())
    }
} 