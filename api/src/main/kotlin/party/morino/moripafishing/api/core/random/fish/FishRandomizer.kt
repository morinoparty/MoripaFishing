package party.morino.moripafishing.api.core.random.fish

import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 魚のランダム化を行うインターフェース
 */
interface FishRandomizer {

    /**
     * 魚をランダムに選択する
     * @return 選択された魚
     */
    fun getRandomFish(fishingWorldId: FishingWorldId): Fish {
        // レアリティをランダムに取得し、そのレアリティに基づいて魚を選択する
        return getRandomFishWithRarity(getRandomRarity(), fishingWorldId)
    }

    /**
     * レアリティに基づいて魚をランダムに選択する
     * @param rarity レアリティ
     * @return 選択された魚
     */
    fun getRandomFishWithRarity(rarity: RarityId, fishingWorldId: FishingWorldId): Fish {
        return getRandomFishWithRarity(rarity, fishingWorldId)
    }

    /**
     * 魚データに基づいて魚をランダムに選択する
     * @param fishData 魚データ
     * @return 選択された魚
     */
    fun getRandomFishWithFishData(fishData: FishData, fishingWorldId: FishingWorldId): Fish

    /**
     * レアリティをランダムに選択する
     * @return 選択されたレアリティ
     */
    fun getRandomRarity(): RarityId

}