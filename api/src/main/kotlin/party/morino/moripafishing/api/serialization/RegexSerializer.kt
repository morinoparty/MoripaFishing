package party.morino.moripafishing.api.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Regexオブジェクトのシリアライザー
 * Regexを文字列として保存・復元する
 */
object RegexSerializer : KSerializer<Regex> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Regex", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Regex,
    ) {
        encoder.encodeString(value.pattern)
    }

    override fun deserialize(decoder: Decoder): Regex {
        return decoder.decodeString().toRegex()
    }
}
