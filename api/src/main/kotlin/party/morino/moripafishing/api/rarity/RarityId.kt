package party.morino.moripafishing.api.rarity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject
import party.morino.moripafishing.api.model.RarityData

@Serializable(with = RarityIdSerializer::class)
class RarityId(val value: String) : KoinComponent { // value class からクラスに変更
    private val rarityManager: RarityManager by inject() // 依存性注入をここで使用
    fun toRarityData(): RarityData {
        return rarityManager.getRarity(this) ?: throw IllegalArgumentException("Invalid rarity value: $value")
    }

    override fun toString(): String {
        return "RarityId(value='$value')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RarityId) return false

        if (value != other.value) return false

        return true
    }
}


object RarityIdSerializer : KSerializer<RarityId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RarityId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: RarityId) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): RarityId {
        return RarityId(decoder.decodeString())
    }
}