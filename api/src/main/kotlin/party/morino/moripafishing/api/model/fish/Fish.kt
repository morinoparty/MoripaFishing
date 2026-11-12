package party.morino.moripafishing.api.model.fish

import party.morino.moripafishing.api.model.rarity.RarityData

/**
 * 抽選済みの魚 (不変値)。`FishDrawer` が返す。
 *
 * @property data 魚の定義
 * @property rarity 魚のレアリティ
 * @property size サイズ (cm)。`data.size` の範囲内
 * @property lengthRate サイズの累積分布関数の値 (0..1)。大きいほど同種の中で大きい
 * @property worth 価値
 */
data class Fish(
    val data: FishData,
    val rarity: RarityData,
    val size: Double,
    val lengthRate: Double,
    val worth: Double,
)
