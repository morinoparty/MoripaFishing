package party.morino.moripafishing.core.random.fish

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.core.random.fish.FishRandomizer
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.core.fish.FishBuilderImpl
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * 魚のランダム化を行う実装クラス
 */
class FishRandomizerImpl : FishRandomizer, KoinComponent {
    private val random = Random()
    private val fishManager: FishManager by inject()
    private val rarityManager: RarityManager by inject()
    private val worldManager: WorldManager by inject()

    private fun getRandomFishDataWithRarity(rarity: RarityId, fishingWorldId: FishingWorldId): FishData {
        val weatherType = worldManager.getWorld(fishingWorldId).getCurrentWeather()
        val fishesData = fishManager.getFishesWithRarity(rarity).filter {
            it.isDisabled == false &&
            (it.conditions.world.isEmpty() || it.conditions.world.contains(fishingWorldId))
            && (it.conditions.weather.isEmpty() || it.conditions.weather.contains(weatherType))
        }
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
    override fun getRandomFishWithRarity(rarity: RarityId, fishingWorldId: FishingWorldId): Fish {
        return getRandomFishWithFishData(getRandomFishDataWithRarity(rarity, fishingWorldId), fishingWorldId)
    }

    override fun getRandomFishWithFishData(fishData: FishData, fishingWorldId: FishingWorldId): Fish {
        val (min, max) = fishData.size
        val mid = (min + max) / 2
        val standardDeviation = (max - min) / 6.0
        val random = ThreadLocalRandom.current().nextGaussian() * standardDeviation + mid
        val size = random.coerceIn(min, max)
        val fish = FishBuilderImpl.getBuilder()
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