package party.morino.moripafishing.core.fish

import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.core.fish.FishBuilder
import party.morino.moripafishing.api.model.fish.FishData

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

    companion object{
        fun getBuilder(): FishBuilder {
            return FishBuilderImpl()
        }
    }
}
