package party.morino.moripafishing.listener

import party.morino.moripafishing.api.core.fish.FishDrawPool
import party.morino.moripafishing.api.core.fish.FishDrawer
import party.morino.moripafishing.api.model.fish.Fish
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 空の候補を作り、抽選では常に [fish] を返す [FishDrawer]。抽選に渡された候補を [drawnPools] に記録する。
 */
internal class FakeFishDrawer(
    var fish: Fish?,
) : FishDrawer {
    val drawnPools = mutableListOf<FishDrawPool>()

    override fun createPool(world: FishingWorldId): FishDrawPool = FishDrawPool(world, null, emptyList(), emptyList())

    override fun draw(pool: FishDrawPool): Fish? {
        drawnPools += pool
        return fish
    }

    override fun roll(data: FishData): Fish = checkNotNull(fish)
}
