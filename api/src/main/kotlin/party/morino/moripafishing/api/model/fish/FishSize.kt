package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.Serializable

/**
 * 魚のサイズの範囲 (cm)。抽選は平均 `(min + max) / 2`・標準偏差 `(max - min) / 6` の正規分布を範囲内に収めて行う。
 *
 * @property min 最小サイズ
 * @property max 最大サイズ。[min] より大きいこと
 */
@Serializable
data class FishSize(
    val min: Double,
    val max: Double,
) {
    init {
        require(min < max) { "size.min must be less than size.max: $min >= $max" }
    }
}
