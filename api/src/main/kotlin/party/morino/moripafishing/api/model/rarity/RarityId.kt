package party.morino.moripafishing.api.model.rarity

import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.utils.serializer.RarityIdSerializer

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

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + rarityManager.hashCode()
        return result
    }
}
