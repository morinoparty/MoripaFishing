package party.morino.moripafishing.api.model.angler

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 釣り人のIDを表すデータクラス
 * @property uuid プレイヤーのUUID
 */
@Serializable
data class AnglerId(
    val uuid: @Serializable(with = UUIDSerializer::class) UUID
) {
    // UUIDをシリアライズ・デシリアライズするためのカスタムシリアライザ
    @Serializable
    object UUIDSerializer : kotlinx.serialization.KSerializer<UUID> {
        override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("UUID", kotlinx.serialization.descriptors.PrimitiveKind.STRING)
        
        override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: UUID) {
            encoder.encodeString(value.toString())
        }
        
        override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): UUID {
            return UUID.fromString(decoder.decodeString())
        }
    }
} 