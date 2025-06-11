package party.morino.moripafishing.core.random.fish

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.api.model.world.Spot
import party.morino.moripafishing.mocks.angler.AnglerMock
import party.morino.moripafishing.mocks.world.FishingWorldMock

/**
 * FishProbabilityManagerImplのテストクラス
 * 魚とレアリティの確率修正機能をテストする
 */
@ExtendWith(MoripaFishingTest::class)
class FishProbabilityManagerImplTest : KoinTest {
    private lateinit var fishProbabilityManager: FishProbabilityManagerImpl
    private lateinit var angler: AnglerMock
    private lateinit var fishingWorld: FishingWorldMock

    // 実際のマネージャーを注入
    private val fishManager: FishManager by inject()
    private val rarityManager: RarityManager by inject()
    private val worldManager: WorldManager by inject()

    // 実際のデータを使用
    private lateinit var testRarityId: RarityId
    private lateinit var testFishId: FishId
    private lateinit var testWorldId: FishingWorldId

    @BeforeEach
    fun setUp() {
        fishProbabilityManager = FishProbabilityManagerImpl()

        // 実際のデータを取得
        testRarityId = rarityManager.getRarities().firstOrNull()?.id ?: RarityId("common")
        testFishId = fishManager.getFish().firstOrNull()?.id ?: FishId("sillago_japonica")
        testWorldId = worldManager.getDefaultWorldId()

        // 基本重みを設定（設定ファイルからの値を模擬）
        fishProbabilityManager.setBaseRarityWeight(testRarityId, 2.0)
        fishProbabilityManager.setBaseFishWeight(testFishId, 2.0)

        // アングラーを設定
        val anglerId = AnglerId(java.util.UUID.randomUUID())
        angler = AnglerMock(anglerId)
        fishingWorld = FishingWorldMock(testWorldId)
        angler.setTestWorld(fishingWorld)
        angler.setTestLocation(Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0))
    }

    @Test
    @DisplayName("FishProbabilityManager No.1: レアリティの確率修正が正しく適用される")
    fun testRarityModificationCorrectlyApplied() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")
        val baseWeight = 10.0

        // レアリティ修正値を適用
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証
        assertEquals(4.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager No.2: 魚の確率修正が正しく適用される")
    fun testFishModificationCorrectlyApplied() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.ADD, 5.0, "")
        val baseWeight = 10.0

        // 魚修正値を適用
        fishProbabilityManager.applyFishModifierForAngler(anglerId, testFishId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedFishWeight(angler, testFishId)

        // 検証
        assertEquals(7.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager No.3: スポット範囲内での修正値が適用される")
    fun testSpotModificationAppliedWithinRange() {
        // 準備
        val location = Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0)
        val spot = Spot(location, 20.0) // 半径20の範囲
        angler.setTestLocation(Location(testWorldId, 15.0, 0.0, 15.0, 0.0, 0.0)) // 範囲内

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")
        val baseWeight = 10.0

        // スポット修正値を適用
        fishProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証
        assertEquals(6.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager No.4: スポット範囲外では修正値が適用されない")
    fun testSpotModificationNotAppliedOutsideRange() {
        // 準備
        val location = Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0)
        val spot = Spot(location, 5.0) // 半径5の範囲
        angler.setTestLocation(Location(testWorldId, 20.0, 0.0, 20.0, 0.0, 0.0)) // 範囲外

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")
        val baseWeight = 10.0

        // スポット修正値を適用
        fishProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証（修正値が適用されず、元の値のまま）
        assertEquals(2.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager No.5: 複数の修正値が重複適用される")
    fun testMultipleModifiersAppliedTogether() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val baseWeight = 10.0

        // 複数の修正値を適用
        fishProbabilityManager.applyRarityModifierForWorld(testWorldId, testRarityId, ApplyValue(ApplyType.MULTIPLY, 2.0, ""))
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, ApplyValue(ApplyType.ADD, 5.0, ""))

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証（2 * 2 + 5 = 9）
        assertEquals(9.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager No.6: CONSTANT修正値が正しく適用される")
    fun testConstantModificationCorrectlyApplied() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.CONSTANT, 50.0, "")
        val baseWeight = 10.0

        // 修正値を適用
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証（CONSTANTでは元の値が無視される）
        assertEquals(50.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager No.7: アングラー修正値のクリアが正常に動作する")
    fun testAnglerModifierClearWorksCorrectly() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")
        val baseWeight = 10.0

        // 修正値を適用
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue)

        // クリア前の確認
        assertEquals(4.0, fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId), 0.001)

        // クリア実行
        fishProbabilityManager.clearAnglerModifiers(anglerId)

        // クリア後の確認
        assertEquals(2.0, fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId), 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager No.8: ロッドタイプによるレアリティ確率修正が正しく適用される")
    fun testRodRarityModificationCorrectlyApplied() {
        // 準備
        val rodType = "legendary"
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")
        val baseWeight = 10.0

        // ロッド修正値を適用
        fishProbabilityManager.applyRarityModifierForRod(rodType, testRarityId, applyValue)

        // テスト用のロッド設定を設定
        val rodConfig = RodConfiguration(rodType = rodType)
        angler.setTestRodConfiguration(rodConfig)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証（10 * 3.0 = 30.0）
        assertEquals(6.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager No.9: ロッドタイプによる魚確率修正が正しく適用される")
    fun testRodFishModificationCorrectlyApplied() {
        // 準備
        val rodType = "speedster"
        val applyValue = ApplyValue(ApplyType.ADD, 15.0, "")
        val baseWeight = 10.0

        // ロッド修正値を適用
        fishProbabilityManager.applyFishModifierForRod(rodType, testFishId, applyValue)

        // テスト用のロッド設定を設定
        val rodConfig = RodConfiguration(rodType = rodType)
        angler.setTestRodConfiguration(rodConfig)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedFishWeight(angler, testFishId)

        // 検証（2 + 15.0 = 17.0）
        assertEquals(17.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager No.10: 確率修正による実際の釣れる確率の変化を可視化")
    fun testProbabilityChangeVisualization() {
        // テスト回数
        val totalTrials = 10000

        // ベースライン（修正なし）の釣果を測定
        val baselineResults = mutableMapOf<FishId, Int>()
        repeat(totalTrials) {
            val weight = fishProbabilityManager.getModifiedFishWeight(angler, testFishId)
            // 正しい重み付き抽選：固定された全体重みで正規化
            val totalPossibleWeight = 20.0 // 仮想的な全体重み
            val probability = weight / totalPossibleWeight
            if (kotlin.random.Random.nextDouble() < probability) {
                baselineResults[testFishId] = baselineResults.getOrDefault(testFishId, 0) + 1
            }
        }

        // 確率修正値を適用（3倍にする）
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")
        fishProbabilityManager.applyFishModifierForAngler(angler.getAnglerUniqueId(), testFishId, applyValue)

        // 修正後の釣果を測定
        val modifiedResults = mutableMapOf<FishId, Int>()
        repeat(totalTrials) {
            val weight = fishProbabilityManager.getModifiedFishWeight(angler, testFishId)
            // 正しい重み付き抽選：固定された全体重みで正規化
            val totalPossibleWeight = 20.0 // 仮想的な全体重み
            val probability = weight / totalPossibleWeight
            if (kotlin.random.Random.nextDouble() < probability) {
                modifiedResults[testFishId] = modifiedResults.getOrDefault(testFishId, 0) + 1
            }
        }

        // 結果の表示
        val baselineCount = baselineResults.getOrDefault(testFishId, 0)
        val modifiedCount = modifiedResults.getOrDefault(testFishId, 0)
        val baselineRate = (baselineCount.toDouble() / totalTrials * 100)
        val modifiedRate = (modifiedCount.toDouble() / totalTrials * 100)

        println("=== 確率修正効果の可視化 (試行回数: $totalTrials) ===")
        println("魚ID: ${testFishId.value}")
        println("修正前: $baselineCount 匹 (${String.format("%.2f", baselineRate)}%)")
        println("修正後: $modifiedCount 匹 (${String.format("%.2f", modifiedRate)}%)")
        println("倍率: ${String.format("%.2f", modifiedRate / baselineRate)}倍")

        // 修正値が適用されて釣果が増加していることを確認
        assertTrue(modifiedCount > baselineCount, "修正値適用後の釣果が増加していません")
    }

    @Test
    @DisplayName("FishProbabilityManager No.11: 期限付き修正値が時間経過で無効になる")
    fun testTimeLimitedModifierExpiration() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 5.0, "")
        val baseWeight = 10.0

        // 50ミリ秒後に期限切れになる修正値を適用
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue, 50L)

        // 即座に確認（修正値が適用されている）
        val immediateWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(10.0, immediateWeight, 0.001, "修正値が即座に適用されていません")

        // 100ミリ秒待機
        Thread.sleep(100)

        // 期限切れ後の確認（修正値が無効になっている）
        val expiredWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(2.0, expiredWeight, 0.001, "期限切れ修正値が自動削除されていません")

        println("=== 時間制限テスト結果 ===")
        println("即座: $immediateWeight (修正値適用)")
        println("100ms後: $expiredWeight (修正値期限切れ)")
    }

    @Test
    @DisplayName("FishProbabilityManager No.12: 複数の修正値による段階的確率変化の可視化")
    fun testMultipleModifierVisualization() {
        val totalTrials = 5000
        val anglerId = angler.getAnglerUniqueId()

        println("=== 段階的確率変化の可視化 (試行回数: $totalTrials) ===")

        // ステップ1: ベースライン
        var results = measureFishCatchRate(totalTrials)
        println("1. ベースライン: ${results.first} 匹 (${String.format("%.2f", results.second)}%)")

        // ステップ2: World修正値追加（2倍）
        fishProbabilityManager.applyRarityModifierForWorld(testWorldId, testRarityId, ApplyValue(ApplyType.MULTIPLY, 2.0, ""))
        results = measureFishCatchRate(totalTrials)
        println("2. World修正(x2): ${results.first} 匹 (${String.format("%.2f", results.second)}%)")

        // ステップ3: Angler修正値追加（+5）
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, ApplyValue(ApplyType.ADD, 5.0, ""))
        results = measureFishCatchRate(totalTrials)
        println("3. + Angler修正(+5): ${results.first} 匹 (${String.format("%.2f", results.second)}%)")

        // ステップ4: Spot修正値追加（1.5倍）
        val location = Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0)
        val spot = Spot(location, 20.0)
        angler.setTestLocation(location)
        fishProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, ApplyValue(ApplyType.MULTIPLY, 1.5, ""))
        results = measureFishCatchRate(totalTrials)
        println("4. + Spot修正(x1.5): ${results.first} 匹 (${String.format("%.2f", results.second)}%)")

        // ステップ5: Rod修正値追加（固定20）
        val rodConfig = RodConfiguration(rodType = "legendary")
        angler.setTestRodConfiguration(rodConfig)
        fishProbabilityManager.applyRarityModifierForRod("legendary", testRarityId, ApplyValue(ApplyType.CONSTANT, 20.0, ""))
        results = measureFishCatchRate(totalTrials)
        println("5. + Rod修正(=20): ${results.first} 匹 (${String.format("%.2f", results.second)}%)")
    }

    /**
     * 指定された試行回数での魚の釣獲率を測定するヘルパー関数
     */
    private fun measureFishCatchRate(totalTrials: Int): Pair<Int, Double> {
        var catchCount = 0
        repeat(totalTrials) {
            val weight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
            // 正しい重み付き抽選：固定された全体重みで正規化
            val totalPossibleWeight = 20.0 // 仮想的な全体重み
            val probability = weight / totalPossibleWeight
            if (kotlin.random.Random.nextDouble() < probability) {
                catchCount++
            }
        }
        val catchRate = (catchCount.toDouble() / totalTrials * 100)
        return Pair(catchCount, catchRate)
    }
}
