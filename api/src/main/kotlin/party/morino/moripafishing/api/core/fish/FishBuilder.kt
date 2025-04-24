package party.morino.moripafishing.api.core.fish

import party.morino.moripafishing.api.model.fish.FishData

/**
 * FishのBuilderインターフェース
 */
interface FishBuilder {
    /**
     * FishDataを設定する
     * @param fishData 魚のデータ
     * @return このBuilder
     */
    fun fishData(fishData: FishData): FishBuilder

    /**
     * 魚のサイズを設定する
     * @param size 魚のサイズ
     * @return このBuilder
     */
    fun size(size: Double): FishBuilder

    /**
     * Fishインスタンスを生成する
     * @return 生成されたFishインスタンス
     */
    fun build(): Fish
}
