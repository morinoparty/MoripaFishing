package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.utils.serializer.FishIdSerializer

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

    fun toTranslateKey(): String {
        return "moripa_fishing.fish.$value.name"
    }
}
