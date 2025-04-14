package party.morino.moripafishing.api.fish

import party.morino.moripafishing.api.model.FishData
import party.morino.moripafishing.api.model.RarityData
import party.morino.moripafishing.api.rarity.RarityId
import party.morino.moripafishing.api.fish.FishId

/**
 * 魚の管理を行うインターフェース
 */
interface FishManager {
    /**
     * 魚を登録する
     * @param fish 登録する魚
     */
    fun registerFish(fish: FishData)


    /**
     * 登録されている魚の一覧を取得する
     * @return 魚の一覧
     */
    fun getFish(): List<FishData>


    /**
     * 魚を取得する
     * @param id 魚のID
     * @return 魚
     */
    fun getFishWithId(id: FishId): FishData?

    /**
     * 魚を取得する
     * @param rarity レアリティ
     * @return 魚
     */
    fun getFishesWithRarity(rarity: RarityId): List<FishData>

}