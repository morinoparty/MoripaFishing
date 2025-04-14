package party.morino.moripafishing.fish

import party.morino.moripafishing.api.fish.Fish
import party.morino.moripafishing.api.model.FishData

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

/**
 * FishBuilderの実装クラス
 */
class FishBuilderImpl : FishBuilder {
    private lateinit var fishData: FishData
    private var size: Double = 0.0

    override fun fishData(fishData: FishData): FishBuilder {
        this.fishData = fishData
        return this
    }

    override fun size(size: Double): FishBuilder {
        this.size = size
        return this
    }

    override fun build(): Fish {
        return FishImpl(fishData, size)
    }
}

/**
 * FishBuilderのファクトリ関数
 * @return 新しいFishBuilderインスタンス
 */
fun fishBuilder(): FishBuilder = FishBuilderImpl() 