package party.morino.moripafishing.api.core.fish

import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId

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

    fun unloadFishes()

    fun loadFishes()
}
