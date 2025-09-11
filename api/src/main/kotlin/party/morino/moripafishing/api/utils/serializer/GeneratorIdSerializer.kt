package party.morino.moripafishing.api.utils.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import party.morino.moripafishing.api.model.world.generator.GeneratorId

/**
 * GeneratorIdのシリアライザー
 */
object GeneratorIdSerializer : KSerializer<GeneratorId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GeneratorId", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: GeneratorId,
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): GeneratorId = GeneratorId(decoder.decodeString())
}
