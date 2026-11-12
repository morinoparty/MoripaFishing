package party.morino.moripafishing.core.fish

import party.morino.moripafishing.api.core.fish.FishDrawer
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.fish.Fish
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.world.FishingWorldId
import java.util.Random
import java.util.random.RandomGenerator

/**
 * [FishDrawer] の実装。
 *
 * 条件を満たす魚がいるレアリティだけを候補にしてレアリティを抽選し、その中から魚を抽選する。
 * 実行時に登録解除されたレアリティの魚は抽選されない。
 *
 * @property fishes 魚の定義
 * @property rarities レアリティの定義
 * @property worlds 釣りワールドの天候の参照先
 * @property random 乱数生成器 (テストで差し替える)
 */
internal class FishDrawerImpl(
    private val fishes: FishManager,
    private val rarities: RarityManager,
    private val worlds: WorldManager,
    private val random: RandomGenerator = Random(),
) : FishDrawer {
    override fun draw(world: FishingWorldId): Fish? {
        val weather = worlds.getWorld(world)?.getWeather()
        val eligible =
            fishes
                .getAll()
                .filter { it.enabled && it.weight > 0.0 && it.conditions.matches(world, weather) }
                .groupBy { it.rarity }
        val rarity = rarities.getAll().filter { it.id in eligible }.pickWeighted(random) { it.weight } ?: return null
        val data = eligible.getValue(rarity.id).pickWeighted(random) { it.weight } ?: return null
        return roll(data, rarity)
    }

    override fun roll(data: FishData): Fish {
        val rarity = rarities.get(data.rarity)
        requireNotNull(rarity) { "Rarity '${data.rarity.value}' of fish '${data.id.value}' is not registered" }
        return roll(data, rarity)
    }

    private fun roll(
        data: FishData,
        rarity: RarityData,
    ): Fish {
        val size = SizeDistribution.sample(data.size, random)
        val lengthRate = SizeDistribution.lengthRate(data.size, size)
        val worth = WorthFormula(data.worth ?: rarity.worth).evaluate(lengthRate)
        return Fish(data, rarity, size, lengthRate, worth)
    }
}
