package party.morino.moripafishing.api.model.world

import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Namespaced
import party.morino.moripafishing.api.utils.FishingWorldIdSerializer

/**
 * ワールドのIDを表すクラス
 * @param value ワールドのID
 */
@Serializable(with = FishingWorldIdSerializer::class)
class FishingWorldId(val value: String) {
    override fun toString(): String {
        return "FishingWorldId(value='$value')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FishingWorldId) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

}