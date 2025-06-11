package party.morino.moripafishing.api.core.random.fish

import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 釣りシステムにおける魚の抽選を管理するインターフェース
 * レアリティや釣り場に応じた魚の抽選ロジックを提供する
 */
interface FishRandomizer {
    /**
     * 釣り場に応じた魚を抽選する
     * レアリティの抽選から始まり、そのレアリティに応じた魚を選択する
     *
     * @param fishingWorldId 釣り場のID
     * @return 抽選された魚
     */
    fun selectRandomFish(fishingWorldId: FishingWorldId): Fish {
        // レアリティを抽選し、そのレアリティに基づいて魚を選択する
        return selectRandomFishByRarity(drawRandomRarity(null), fishingWorldId)
    }

    /**
     * 指定されたレアリティに基づいて魚を抽選する
     *
     * @param rarity 抽選対象のレアリティ
     * @param fishingWorldId 釣り場のID
     * @return 抽選された魚
     */
    fun selectRandomFishByRarity(
        rarity: RarityId,
        fishingWorldId: FishingWorldId,
    ): Fish

    /**
     * 魚データに基づいて魚を抽選する
     * 魚データの確率に従って抽選を行う
     *
     * @param fishData 抽選対象の魚データ
     * @return 抽選された魚
     */
    fun selectRandomFishByFishData(fishData: FishData): Fish

    /**
     * レアリティを抽選する
     * 各レアリティの出現確率に従って抽選を行う
     *
     * @param angler 対象の釣り人（nullの場合は確率修正なし）
     * @return 抽選されたレアリティ
     */
    fun drawRandomRarity(angler: Angler? = null): RarityId

    /**
     * 釣り人のコンテキストを考慮して魚を抽選する
     * 確率修正値を適用して抽選を行う
     *
     * @param angler 対象の釣り人
     * @param fishingWorldId 釣り場のID
     * @return 抽選された魚
     */
    fun selectRandomFish(
        angler: Angler,
        fishingWorldId: FishingWorldId,
    ): Fish

    /**
     * 釣り人のコンテキストを考慮して指定レアリティの魚を抽選する
     * 確率修正値を適用して抽選を行う
     *
     * @param angler 対象の釣り人
     * @param rarity 抽選対象のレアリティ
     * @param fishingWorldId 釣り場のID
     * @return 抽選された魚
     */
    fun selectRandomFishByRarity(
        angler: Angler,
        rarity: RarityId,
        fishingWorldId: FishingWorldId,
    ): Fish
}
