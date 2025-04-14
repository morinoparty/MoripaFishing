package party.morino.moripafishing.random.fish

import party.morino.moripafishing.api.fish.Fish
import party.morino.moripafishing.api.fish.FishManager
import party.morino.moripafishing.api.model.RarityData
import party.morino.moripafishing.api.random.fish.FishRandomizer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.model.FishData
import party.morino.moripafishing.api.rarity.RarityId
import party.morino.moripafishing.api.rarity.RarityManager
import party.morino.moripafishing.fish.fishBuilder
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

/**
 * 魚のランダム化を行う実装クラス
 */
class FishRandomizerImpl : FishRandomizer, KoinComponent {
    private val random = Random()
    private val fishManager: FishManager by inject()
    private val rarityManager: RarityManager by inject()

    private fun getRandomFishDataWithRarity(rarity: RarityId): FishData {
        val fishesData = fishManager.getFishesWithRarity(rarity)
        val total = fishesData.sumOf { it.weight }
        val randomValue = random.nextDouble() * total
        var sum = 0.0
        for (fish in fishesData) {
            sum += fish.weight
            if (randomValue <= sum) {
                return fish
            }
        }
        return fishesData.last()
    }

    /**
     * レアリティに基づいて魚をランダムに選択する
     * @param rarity レアリティ
     * @return 選択された魚
     */
    override fun getRandomFishWithRarity(rarity: RarityId): Fish {
        return getRandomFishWithFishData(getRandomFishDataWithRarity(rarity))
    }

    override fun getRandomFishWithFishData(fishData: FishData): Fish {
        val (min, max) = fishData.size
        val mid = (min + max) / 2
        val standardDeviation = (max - min) / 6.0
        val random = ThreadLocalRandom.current().nextGaussian() * standardDeviation + mid
        val size = random.coerceIn(min, max)
        val fish = fishBuilder()
            .fishData(fishData)
            .size(size)
            .build()
        return fish
    }

    /**
     * レアリティをランダムに選択する
     * @return 選択されたレアリティ
     */
    override fun getRandomRarity(): RarityId {
        val rarities = rarityManager.getRarities()
        val total = rarities.sumOf { it.weight }
        val randomValue = random.nextDouble() * total
        var sum = 0.0
        for (rarity in rarities) {
            sum += rarity.weight
            if (randomValue <= sum) {
                return rarity.id
            }
        }
        return rarities.last().id
    }
} 