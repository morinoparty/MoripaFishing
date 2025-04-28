package party.morino.moripafishing.api.core.fish

import net.kyori.adventure.text.Component
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityData
import java.util.Locale

/**
 * 魚を表すインターフェース
 */
interface Fish {
    /**
     * 魚のキーを取得する
     * @return 魚のキー
     */
    fun getId(): FishId

    /**
     * 魚の表示名を取得する
     * @return 魚の表示名
     */
    fun getDisplayName(): Map<Locale, Component>

    /**
     * 魚のサイズを取得する
     * @return 魚のサイズ
     */
    fun getSize(): Double

    /**
     * 魚のレアリティを取得する
     * @return 魚のレアリティ
     */
    fun getRarity(): RarityData

    /**
     * 魚の価値を取得する
     * @return 魚の価値
     */
    fun getWorth(): Double

    /**
     * 魚の累積分布関数を取得する
     * @return 魚の累積分布関数 0~1の値
     */
    fun getCFD(): Double
}
