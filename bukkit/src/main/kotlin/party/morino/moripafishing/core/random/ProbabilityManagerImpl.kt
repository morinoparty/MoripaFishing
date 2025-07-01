package party.morino.moripafishing.core.random

import org.koin.core.component.KoinComponent
import party.morino.moripafishing.api.core.random.ProbabilityManager
import party.morino.moripafishing.api.core.random.fish.FishProbabilityManager
import party.morino.moripafishing.api.core.random.rarity.RarityProbabilityManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.core.random.fish.FishProbabilityManagerImpl
import party.morino.moripafishing.core.random.rarity.RarityProbabilityManagerImpl

/**
 * 魚とレアリティの確率管理を統合するファサード実装クラス
 * FishProbabilityManagerとRarityProbabilityManagerへのアクセスを提供し、
 * 既存のコードとの互換性を保つ
 */
class ProbabilityManagerImpl : ProbabilityManager, KoinComponent {
    // 各専門マネージャーを注入
    private val fishManager: FishProbabilityManager = FishProbabilityManagerImpl()
    private val rarityManager: RarityProbabilityManager = RarityProbabilityManagerImpl()

    override fun getFishProbabilityManager(): FishProbabilityManager = fishManager

    override fun getRarityProbabilityManager(): RarityProbabilityManager = rarityManager

    override fun clearAnglerModifiers(anglerId: AnglerId) {
        // 魚とレアリティの両方のマネージャーから修正値をクリア
        fishManager.clearAnglerFishModifiers(anglerId)
        rarityManager.clearAnglerRarityModifiers(anglerId)
    }

    override fun cleanupExpiredModifiers() {
        // 魚とレアリティの両方のマネージャーで期限切れ修正値をクリーンアップ
        fishManager.cleanupExpiredFishModifiers()
        rarityManager.cleanupExpiredRarityModifiers()
    }
}
