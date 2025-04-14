package party.morino.moripafishing.api.random.fish

import party.morino.moripafishing.api.fish.Fish
import party.morino.moripafishing.api.model.FishData
import party.morino.moripafishing.api.model.RarityData
import party.morino.moripafishing.api.rarity.RarityId

/**
 * 魚のランダム化を行うインターフェース
 */
interface FishRandomizer {

    /**
     * 魚をランダムに選択する
     * @return 選択された魚
     */
    fun getRandomFish(): Fish {
        // レアリティをランダムに取得し、そのレアリティに基づいて魚を選択する
        return getRandomFishWithRarity(getRandomRarity())
    }

    /**
     * レアリティに基づいて魚をランダムに選択する
     * @param rarity レアリティ
     * @return 選択された魚
     */
    fun getRandomFishWithRarity(rarity: RarityId): Fish{
        return getRandomFishWithRarity(rarity)
    }

    /**
     * 魚データに基づいて魚をランダムに選択する
     * @param fishData 魚データ
     * @return 選択された魚
     */
    fun getRandomFishWithFishData(fishData: FishData): Fish

    /**
     * レアリティをランダムに選択する
     * @return 選択されたレアリティ
     */
    fun getRandomRarity(): RarityId

}