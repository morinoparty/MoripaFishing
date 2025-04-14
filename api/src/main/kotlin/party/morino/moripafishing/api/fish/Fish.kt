package party.morino.moripafishing.api.fish

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import party.morino.moripafishing.api.model.FishData
import party.morino.moripafishing.api.model.RarityData
import party.morino.moripafishing.api.fish.FishId
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
} 