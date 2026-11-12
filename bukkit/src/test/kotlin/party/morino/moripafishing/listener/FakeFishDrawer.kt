package party.morino.moripafishing.listener

import party.morino.moripafishing.api.core.fish.FishDrawer
import party.morino.moripafishing.api.model.fish.Fish
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 常に [fish] を返す [FishDrawer]。
 */
internal class FakeFishDrawer(
    var fish: Fish?,
) : FishDrawer {
    override fun draw(world: FishingWorldId): Fish? = fish

    override fun roll(data: FishData): Fish = checkNotNull(fish)
}
