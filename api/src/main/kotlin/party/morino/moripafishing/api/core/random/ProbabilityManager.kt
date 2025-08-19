package party.morino.moripafishing.api.core.random

import party.morino.moripafishing.api.core.random.fish.FishProbabilityManager
import party.morino.moripafishing.api.core.random.rarity.RarityProbabilityManager
import party.morino.moripafishing.api.model.angler.AnglerId

/**
 * 魚とレアリティの確率管理を統合するファサードクラス
 * 既存のコードとの互換性を保つために、各マネージャーへのアクセスを提供する
 */
interface ProbabilityManager {
    /**
     * 魚の確率管理マネージャーを取得する
     *
     * @return FishProbabilityManager
     */
    fun getFishProbabilityManager(): FishProbabilityManager

    /**
     * レアリティの確率管理マネージャーを取得する
     *
     * @return RarityProbabilityManager
     */
    fun getRarityProbabilityManager(): RarityProbabilityManager

    /**
     * 特定の釣り人に適用されている修正値をクリアする
     * 魚とレアリティの両方のマネージャーの修正値をクリアする
     *
     * @param anglerId 対象の釣り人ID
     */
    fun clearAnglerModifiers(anglerId: AnglerId)

    /**
     * 期限切れの修正値をクリーンアップする
     * 魚とレアリティの両方のマネージャーの期限切れ修正値をクリーンアップする
     */
    fun cleanupExpiredModifiers()
}
