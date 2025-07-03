package party.morino.moripafishing.core.random.fish

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.random.ProbabilityManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.mocks.angler.AnglerMock
import party.morino.moripafishing.mocks.world.FishingWorldMock

/**
 * FishProbabilityManagerImplの統計的テストクラス
 * 魚確率修正機能の効果を統計的に検証する
 */
@ExtendWith(MoripaFishingTest::class)
class FishProbabilityManagerStatisticalTest : KoinTest {
    private val probabilityManager: ProbabilityManager by inject()
    private val fishProbabilityManager by lazy { probabilityManager.getFishProbabilityManager() }
    private lateinit var angler: AnglerMock
    private lateinit var fishingWorld: FishingWorldMock

    // 実際のマネージャーを注入
    private val fishManager: FishManager by inject()
    private val rarityManager: RarityManager by inject()
    private val worldManager: WorldManager by inject()
    private val randomizeManager: RandomizeManager by inject()
    private val fishRandomizer by lazy { randomizeManager.getFishRandomizer() }

    // 実際のデータを使用
    private lateinit var testRarityId: RarityId
    private lateinit var testFishId: FishId
    private lateinit var testWorldId: FishingWorldId

    @BeforeEach
    fun setUp() {
        // 実際のデータを取得
        testRarityId = rarityManager.getRarities().firstOrNull()?.id ?: RarityId("common")
        testFishId = fishManager.getFish().firstOrNull()?.id ?: FishId("sillago_japonica")
        testWorldId = worldManager.getDefaultWorldId()

        // アングラーを設定
        val anglerId = AnglerId(java.util.UUID.randomUUID())
        angler = AnglerMock(anglerId)
        fishingWorld = FishingWorldMock(testWorldId)
        angler.setTestWorld(fishingWorld)
    }

    @Test
    @DisplayName("FishProbabilityManager Statistical No.1: 魚確率修正の基本機能を統計的に検証")
    fun testFishProbabilityModificationSystemBasicFunctionality() {
        val totalTrials = 1000
        val anglerId = angler.getAnglerUniqueId()

        println("=== 魚確率修正システム基本機能検証 (試行回数: $totalTrials) ===")

        // Step 1: ベースライン測定
        println("1. ベースライン測定中...")
        val baselineStats = getFishStats(totalTrials, angler)
        val baselineTargetRate = baselineStats.getOrDefault(testFishId, 0).toDouble() / totalTrials

        // Step 2: 魚修正値適用（3倍）
        fishProbabilityManager.applyFishModifierForAngler(anglerId, testFishId, ApplyValue(ApplyType.MULTIPLY, 3.0, "テスト修正"))
        println("2. 魚修正(x3)後測定中...")
        val modifiedStats = getFishStats(totalTrials, angler)
        val modifiedTargetRate = modifiedStats.getOrDefault(testFishId, 0).toDouble() / totalTrials

        // 結果分析
        println("\n=== 分析結果 ===")
        println("ベースライン出現率: ${String.format("%.4f", baselineTargetRate)} (${baselineStats.getOrDefault(testFishId, 0)}/$totalTrials)")
        println("修正後出現率: ${String.format("%.4f", modifiedTargetRate)} (${modifiedStats.getOrDefault(testFishId, 0)}/$totalTrials)")

        val improvementRatio = if (baselineTargetRate > 0) modifiedTargetRate / baselineTargetRate else 0.0
        println("改善倍率: ${String.format("%.2f", improvementRatio)}")

        // 統計的な検証
        assertTrue(modifiedTargetRate >= baselineTargetRate, "修正後の出現率がベースライン以上になるべき")
    }

    @Test
    @DisplayName("FishProbabilityManager Statistical No.2: 単純な重み付き抽選システムの一貫性を検証")
    fun testSimpleWeightedFishRandomSelection() {
        val totalTrials = 500

        println("=== 単純重み付き魚抽選システム検証 (試行回数: $totalTrials) ===")

        // 魚の重み付き抽選を実行
        val fishStats = getFishStats(totalTrials, angler)

        // 結果出力
        println("\n=== 魚抽選結果 ===")
        for ((fishId, count) in fishStats) {
            val rate = count.toDouble() / totalTrials
            println("${fishId.value}: $count 回 (${String.format("%.4f", rate)})")
        }

        // 基本検証: 何らかの魚が出現している
        assertTrue(fishStats.isNotEmpty(), "魚が出現するべき")
        assertTrue(fishStats.values.sum() == totalTrials, "合計試行回数が一致するべき")
    }

    /**
     * 魚統計情報を取得するヘルパーメソッド
     */
    private fun getFishStats(trials: Int, angler : Angler): Map<FishId, Int> {
        val fishCount = mutableMapOf<FishId, Int>()

        repeat(trials) {
            // 二段階抽選を実行
            val rarityId = fishRandomizer.drawRandomRarity(angler)
            val fish = fishRandomizer.selectRandomFishByRarity(rarityId, testWorldId)

            fishCount[fish.getId()] = fishCount.getOrDefault(fish.getId(), 0) + 1
        }

        return fishCount
    }
}
