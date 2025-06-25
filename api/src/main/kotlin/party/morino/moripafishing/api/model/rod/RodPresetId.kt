package party.morino.moripafishing.api.model.rod

import kotlinx.serialization.Serializable

/**
 * ロッドプリセットIDを表すvalue class
 * プリセット名をタイプセーフに扱うために使用する
 *
 * @param value プリセット名（例: "beginner", "master", "legendary"）
 */
@Serializable
@JvmInline
value class RodPresetId(val value: String) : Comparable<RodPresetId> {
    init {
        require(value.isNotBlank()) { "RodPresetId value must not be blank" }
    }

    override fun toString(): String = value

    override fun compareTo(other: RodPresetId): Int = value.compareTo(other.value)
}
