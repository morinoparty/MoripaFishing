package party.morino.moripafishing.api.utils.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Locale

/**
 * Localeのシリアライザ
 * Localeを文字列としてシリアライズする
 */
object LocaleSerializer : KSerializer<Locale> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Locale", PrimitiveKind.STRING)

    /**
     * デシリアライズ
     * @param decoder デコーダー
     * @return デシリアライズされたLocale
     */
    override fun deserialize(decoder: Decoder): Locale {
        val localeFromString = Locale.forLanguageTag(decoder.decodeString().replace('_', '-'))
        return localeFromString
    }

    /**
     * シリアライズ
     * @param encoder エンコーダー
     * @param value シリアライズするLocale
     */
    override fun serialize(
        encoder: Encoder,
        value: Locale,
    ) {
        encoder.encodeString(value.toString())
    }
}
