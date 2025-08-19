package party.morino.moripafishing.core.random.rarity

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.random.ProbabilityManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.api.model.world.Spot
import party.morino.moripafishing.mocks.angler.AnglerMock
import party.morino.moripafishing.mocks.world.FishingWorldMock

/**
 * RarityProbabilityManagerImplの統計的テストクラス
 * レアリティ確率修正機能の効果を統計的に検証する
 */
@ExtendWith(MoripaFishingTest::class)
class RarityProbabilityManagerStatisticalTest : KoinTest {
    private lateinit var angler: AnglerMock
    private lateinit var fishingWorld: FishingWorldMock

    // 実際のマネージャーを注入
    private val rarityManager: RarityManager by inject()
    private val probabilityManager: ProbabilityManager by inject()
    private val rarityProbabilityManager by lazy { probabilityManager.getRarityProbabilityManager() }
    private val worldManager: WorldManager by inject()
    private val randomizeManager: RandomizeManager by inject()
    private val fishRandomizer by lazy { randomizeManager.getFishRandomizer() }

    // 実際のデータを使用
    private lateinit var testRarityId: RarityId
    private lateinit var testWorldId: FishingWorldId

    @BeforeEach
    fun setUp() {
        // 実際のデータを取得
        testRarityId = rarityManager.getRarities().firstOrNull()?.id ?: RarityId("common")
        testWorldId = worldManager.getDefaultWorldId()

        // アングラーを設定
        val anglerId = AnglerId(java.util.UUID.randomUUID())
        angler = AnglerMock(anglerId)
        fishingWorld = FishingWorldMock(testWorldId)
        angler.setTestWorld(fishingWorld)
    }

    @Test
    @DisplayName("RarityProbabilityManager Statistical No.1: レアリティ確率修正の基本機能を統計的に検証")
    fun testRarityProbabilityModificationSystemBasicFunctionality() {
        val totalTrials = 1000
        val anglerId = angler.getAnglerUniqueId()

        println("=== レアリティ確率修正システム基本機能検証 (試行回数: $totalTrials) ===")

        // Step 1: ベースライン測定
        println("1. ベースライン測定中...")
        val baselineStats = getRarityStats(totalTrials)
        val baselineTargetRate = baselineStats.getOrDefault(testRarityId, 0).toDouble() / totalTrials

        // Step 2: レアリティ修正値適用（3倍）
        rarityProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, ApplyValue(ApplyType.MULTIPLY, 3.0, "テスト修正"))
        println("2. レアリティ修正(x3)後測定中...")
        val modifiedStats = getRarityStats(totalTrials)
        val modifiedTargetRate = modifiedStats.getOrDefault(testRarityId, 0).toDouble() / totalTrials

        // 結果分析
        println("\n=== 分析結果 ===")
        println("ベースライン出現率: ${String.format("%.4f", baselineTargetRate)} (${baselineStats.getOrDefault(testRarityId, 0)}/$totalTrials)")
        println("修正後出現率: ${String.format("%.4f", modifiedTargetRate)} (${modifiedStats.getOrDefault(testRarityId, 0)}/$totalTrials)")

        val improvementRatio = if (baselineTargetRate > 0) modifiedTargetRate / baselineTargetRate else 0.0
        println("改善倍率: ${String.format("%.2f", improvementRatio)}")

        // 統計的な検証
        assertTrue(modifiedTargetRate > baselineTargetRate, "修正後の出現率がベースラインより高くなるべき")
        assertTrue(improvementRatio > 1.5, "改善倍率は1.5倍以上になるべき (実際: ${String.format("%.2f", improvementRatio)})")
    }

    @Test
    @DisplayName("RarityProbabilityManager Statistical No.2: 複数修正値の累積効果を統計的に検証")
    fun testCumulativeRarityModifierEffectsWithStatistics() {
        val totalTrials = 2000
        val anglerId = angler.getAnglerUniqueId()

        println("=== 複数レアリティ修正値の累積効果統計検証 (試行回数: $totalTrials) ===")

        // ステップ1: ベースライン測定
        println("1. ベースライン測定中...")
        val baselineStats = getRarityStats(totalTrials)
        val baselineTargetRate = baselineStats.getOrDefault(testRarityId, 0).toDouble() / totalTrials

        // ステップ2: World修正値追加（2倍）
        rarityProbabilityManager.applyRarityModifierForWorld(testWorldId, testRarityId, ApplyValue(ApplyType.MULTIPLY, 2.0, "World修正"))
        println("2. World修正(x2)後測定中...")
        val worldModifiedStats = getRarityStats(totalTrials)
        val worldModifiedRate = worldModifiedStats.getOrDefault(testRarityId, 0).toDouble() / totalTrials

        // ステップ3: Angler修正値追加（+5）
        rarityProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, ApplyValue(ApplyType.ADD, 5.0, "Angler修正"))
        println("3. Angler修正(+5)後測定中...")
        val anglerModifiedStats = getRarityStats(totalTrials)
        val anglerModifiedRate = anglerModifiedStats.getOrDefault(testRarityId, 0).toDouble() / totalTrials

        // ステップ4: Spot修正値追加（1.5倍）
        val location = Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0)
        val spot = Spot(location, 20.0)
        angler.setTestLocation(location)
        rarityProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, ApplyValue(ApplyType.MULTIPLY, 1.5, "Spot修正"))
        println("4. Spot修正(x1.5)後測定中...")
        val spotModifiedStats = getRarityStats(totalTrials)
        val spotModifiedRate = spotModifiedStats.getOrDefault(testRarityId, 0).toDouble() / totalTrials

        // 統計分析結果の出力
        println("\n=== 累積効果分析結果 ===")
        println("ベースライン: ${String.format("%.4f", baselineTargetRate)} (${baselineStats.getOrDefault(testRarityId, 0)}/$totalTrials)")
        println(
            "World修正後: ${String.format(
                "%.4f",
                worldModifiedRate,
            )} (上昇倍率: ${String.format("%.2f", worldModifiedRate / baselineTargetRate)})",
        )
        println(
            "Angler修正後: ${String.format(
                "%.4f",
                anglerModifiedRate,
            )} (上昇倍率: ${String.format("%.2f", anglerModifiedRate / baselineTargetRate)})",
        )
        println(
            "Spot修正後: ${String.format("%.4f", spotModifiedRate)} (上昇倍率: ${String.format("%.2f", spotModifiedRate / baselineTargetRate)})",
        )

        // 検証: 各段階で出現率が向上する
        assertTrue(worldModifiedRate > baselineTargetRate, "World修正で出現率が向上するべき")
        assertTrue(anglerModifiedRate > worldModifiedRate, "Angler修正で更に出現率が向上するべき")
        assertTrue(spotModifiedRate > anglerModifiedRate, "Spot修正で最終的に出現率が向上するべき")
    }

    /**
     * レアリティ統計情報を取得するヘルパーメソッド
     */
    private fun getRarityStats(trials: Int): Map<RarityId, Int> {
        val rarityCount = mutableMapOf<RarityId, Int>()

        repeat(trials) {
            // レアリティ抽選のみを実行
            val rarityId = fishRandomizer.drawRandomRarity(angler)
            rarityCount[rarityId] = rarityCount.getOrDefault(rarityId, 0) + 1
        }

        return rarityCount
    }
}
