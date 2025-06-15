package party.morino.moripafishing.core.random.fish

import org.junit.jupiter.api.Assertions.assertEquals
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
 * FishProbabilityManagerImplの機能テストクラス
 * 魚とレアリティの確率修正機能の基本動作をテストする
 */
@ExtendWith(MoripaFishingTest::class)
class FishProbabilityManagerFunctionalTest : KoinTest {
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

    // 実際のデータの重み
    private var actualRarityWeight: Double = 0.0
    private var actualFishWeight: Double = 0.0

    @BeforeEach
    fun setUp() {
        fishProbabilityManager = FishProbabilityManagerImpl()

        // 実際のデータを取得
        testRarityId = rarityManager.getRarities().firstOrNull()?.id ?: RarityId("common")
        testFishId = fishManager.getFish().firstOrNull()?.id ?: FishId("sillago_japonica")
        testWorldId = worldManager.getDefaultWorldId()

        // 基本重みは初期化時に自動読み込みされる
        // 実際のデータから基本重みを取得
        actualRarityWeight = fishProbabilityManager.getBaseRarityWeight(testRarityId)
        actualFishWeight = fishProbabilityManager.getBaseFishWeight(testFishId)

        // アングラーを設定
        val anglerId = AnglerId(java.util.UUID.randomUUID())
        angler = AnglerMock(anglerId)
        fishingWorld = FishingWorldMock(testWorldId)
        angler.setTestWorld(fishingWorld)
        angler.setTestLocation(Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0))
    }

    @Test
    @DisplayName("FishProbabilityManager Functional No.1: レアリティの確率修正が正しく適用される")
    fun testRarityModificationCorrectlyApplied() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")

        // レアリティ修正値を適用
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証 (実際のレアリティ重み * 2.0)
        assertEquals(actualRarityWeight * 2.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager Functional No.2: 魚の確率修正が正しく適用される")
    fun testFishModificationCorrectlyApplied() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.ADD, 5.0, "")

        // 魚修正値を適用
        fishProbabilityManager.applyFishModifierForAngler(anglerId, testFishId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedFishWeight(angler, testFishId)

        // 検証 (実際の魚重み + 5.0)
        assertEquals(actualFishWeight + 5.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager Functional No.3: スポット範囲内での修正値が適用される")
    fun testSpotModificationAppliedWithinRange() {
        // 準備
        val location = Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0)
        val spot = Spot(location, 20.0) // 半径20の範囲
        angler.setTestLocation(Location(testWorldId, 15.0, 0.0, 15.0, 0.0, 0.0)) // 範囲内
        // 釣り針も同じ位置に設定
        angler.setTestFishingHookLocation(Location(testWorldId, 15.0, 0.0, 15.0, 0.0, 0.0))

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")

        // スポット修正値を適用
        fishProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証 (実際のレアリティ重み * 3.0)
        assertEquals(actualRarityWeight * 3.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager Functional No.4: スポット範囲外では修正値が適用されない")
    fun testSpotModificationNotAppliedOutsideRange() {
        // 準備
        val location = Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0)
        val spot = Spot(location, 5.0) // 半径5の範囲
        angler.setTestLocation(Location(testWorldId, 20.0, 0.0, 20.0, 0.0, 0.0)) // 範囲外

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")

        // スポット修正値を適用
        fishProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証（修正値が適用されず、元の値のまま）
        assertEquals(actualRarityWeight, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager Functional No.5: 複数の修正値が重複適用される")
    fun testMultipleModifiersAppliedTogether() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        // 複数の修正値を適用
        fishProbabilityManager.applyRarityModifierForWorld(testWorldId, testRarityId, ApplyValue(ApplyType.MULTIPLY, 2.0, ""))
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, ApplyValue(ApplyType.ADD, 5.0, ""))

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証（実際の基本重み * 2 + 5）
        assertEquals(actualRarityWeight * 2.0 + 5.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager Functional No.6: CONSTANT修正値が正しく適用される")
    fun testConstantModificationCorrectlyApplied() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.CONSTANT, 50.0, "")

        // 修正値を適用
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証（CONSTANTでは元の値が無視される）
        assertEquals(50.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager Functional No.7: アングラー修正値のクリアが正常に動作する")
    fun testAnglerModifierClearWorksCorrectly() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")

        // 修正値を適用
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue)

        // クリア前の確認
        assertEquals(actualRarityWeight * 2.0, fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId), 0.001)

        // クリア実行
        fishProbabilityManager.clearAnglerModifiers(anglerId)

        // クリア後の確認
        assertEquals(actualRarityWeight, fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId), 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager Functional No.8: ロッドタイプによるレアリティ確率修正が正しく適用される")
    fun testRodRarityModificationCorrectlyApplied() {
        // 準備
        val rodType = "legendary"
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")

        // ロッド修正値を適用
        fishProbabilityManager.applyRarityModifierForRod(rodType, testRarityId, applyValue)

        // テスト用のロッド設定を設定
        val rodConfig = RodConfiguration(rodType = rodType)
        angler.setTestRodConfiguration(rodConfig)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証（実際のレアリティ重み * 3.0）
        assertEquals(actualRarityWeight * 3.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager Functional No.9: ロッドタイプによる魚確率修正が正しく適用される")
    fun testRodFishModificationCorrectlyApplied() {
        // 準備
        val rodType = "speedster"
        val applyValue = ApplyValue(ApplyType.ADD, 15.0, "")

        // ロッド修正値を適用
        fishProbabilityManager.applyFishModifierForRod(rodType, testFishId, applyValue)

        // テスト用のロッド設定を設定
        val rodConfig = RodConfiguration(rodType = rodType)
        angler.setTestRodConfiguration(rodConfig)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedFishWeight(angler, testFishId)

        // 検証（実際の魚重み + 15.0）
        assertEquals(actualFishWeight + 15.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishProbabilityManager Functional No.10: 期限付き修正値が時間経過で無効になる")
    fun testTimeLimitedModifierExpiration() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 5.0, "")

        // 50ミリ秒後に期限切れになる修正値を適用
        fishProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue, 50L)

        // 即座に確認（修正値が適用されている）
        val immediateWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(actualRarityWeight * 5.0, immediateWeight, 0.001, "修正値が即座に適用されていません")

        // 100ミリ秒待機
        Thread.sleep(100)

        // 期限切れ後の確認（修正値が無効になっている）
        val expiredWeight = fishProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(actualRarityWeight, expiredWeight, 0.001, "期限切れ修正値が自動削除されていません")

        println("=== 時間制限テスト結果 ===")
        println("即座: $immediateWeight (修正値適用)")
        println("100ms後: $expiredWeight (修正値期限切れ)")
    }
}
