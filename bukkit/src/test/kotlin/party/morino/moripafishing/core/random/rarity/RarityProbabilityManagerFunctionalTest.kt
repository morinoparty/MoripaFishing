package party.morino.moripafishing.core.random.rarity

import org.junit.jupiter.api.Assertions.assertEquals
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
 * RarityProbabilityManagerの機能テストクラス
 * レアリティの確率修正機能の基本動作をテストする
 */
@ExtendWith(MoripaFishingTest::class)
class RarityProbabilityManagerFunctionalTest : KoinTest {
    private lateinit var angler: AnglerMock
    private lateinit var fishingWorld: FishingWorldMock

    // 実際のマネージャーを注入
    private val probabilityManager: ProbabilityManager by inject()
    private val rarityManager: RarityManager by inject()
    private val rarityProbabilityManager by lazy { probabilityManager.getRarityProbabilityManager() }
    private val worldManager: WorldManager by inject()

    // 実際のデータを使用
    private lateinit var testRarityId: RarityId
    private lateinit var testWorldId: FishingWorldId

    // 実際のデータの重み
    private var actualRarityWeight: Double = 0.0

    @BeforeEach
    fun setUp() {
        // 実際のデータを取得
        testRarityId = rarityManager.getRarities().firstOrNull()?.id ?: RarityId("common")
        testWorldId = worldManager.getDefaultWorldId()

        // 基本重みは初期化時に自動読み込みされる
        // 実際のデータから基本重みを取得
        actualRarityWeight = rarityProbabilityManager.getBaseRarityWeight(testRarityId)

        // アングラーを設定
        val anglerId = AnglerId(java.util.UUID.randomUUID())
        angler = AnglerMock(anglerId)
        fishingWorld = FishingWorldMock(testWorldId)
        angler.setTestWorld(fishingWorld)
        angler.setTestLocation(Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0))
    }

    @Test
    @DisplayName("RarityProbabilityManager Functional No.1: レアリティの確率修正が正しく適用される")
    fun testRarityModificationCorrectlyApplied() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")

        // レアリティ修正値を適用
        rarityProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue)

        // 実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証 (実際のレアリティ重み * 2.0)
        assertEquals(actualRarityWeight * 2.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("RarityProbabilityManager Functional No.2: Spot範囲内でレアリティ修正が適用される")
    fun testSpotModificationAppliedWithinRange() {
        // Spotを作成 (位置: 5, 5, 5, 半径: 10)
        val spotLocation = Location(testWorldId, 5.0, 5.0, 5.0, 0.0, 0.0)
        val spot = Spot(spotLocation, 10.0)

        // プレイヤーをSpot範囲内に配置 (距離約8.66)
        angler.setTestLocation(Location(testWorldId, 10.0, 10.0, 10.0, 0.0, 0.0))

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")

        // Spot修正値を適用
        rarityProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証 (Spot範囲内なので効果が適用される)
        assertEquals(actualRarityWeight * 3.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("RarityProbabilityManager Functional No.3: Spot範囲外でレアリティ修正が適用されない")
    fun testSpotModificationNotAppliedOutsideRange() {
        // Spotを作成 (位置: 0, 0, 0, 半径: 5)
        val spotLocation = Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0)
        val spot = Spot(spotLocation, 5.0)

        // プレイヤーをSpot範囲外に配置 (距離約17.32)
        angler.setTestLocation(Location(testWorldId, 10.0, 10.0, 10.0, 0.0, 0.0))

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")

        // Spot修正値を適用
        rarityProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証 (Spot範囲外なので効果が適用されない)
        assertEquals(actualRarityWeight, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("RarityProbabilityManager Functional No.4: 複数の修正値が同時に適用される")
    fun testMultipleModifiersAppliedTogether() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        // 複数の修正値を適用 (World: *2.0, Angler: +5.0)
        rarityProbabilityManager.applyRarityModifierForWorld(testWorldId, testRarityId, ApplyValue(ApplyType.MULTIPLY, 2.0, ""))
        rarityProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, ApplyValue(ApplyType.ADD, 5.0, ""))

        // 実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証 (実際のレアリティ重み * 2.0 + 5.0)
        assertEquals(actualRarityWeight * 2.0 + 5.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("RarityProbabilityManager Functional No.5: CONSTANT修正が正しく適用される")
    fun testConstantModificationCorrectlyApplied() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.CONSTANT, 10.0, "")

        // レアリティ修正値を適用
        rarityProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue)

        // 実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証 (定数値10.0が適用される)
        assertEquals(10.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("RarityProbabilityManager Functional No.6: Angler修正値のクリアが正しく動作する")
    fun testAnglerModifierClearWorksCorrectly() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")

        // レアリティ修正値を適用
        rarityProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue)

        // クリア前の確認
        assertEquals(actualRarityWeight * 2.0, rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId), 0.001)

        // クリア実行
        probabilityManager.clearAnglerModifiers(anglerId)

        // クリア後の確認
        assertEquals(actualRarityWeight, rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId), 0.001)
    }

    @Test
    @DisplayName("RarityProbabilityManager Functional No.7: 期限付き修正値の有効期限が正しく動作する")
    fun testTimeLimitedModifierExpiration() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")

        // 期限付きレアリティ修正値を適用（50ms）
        rarityProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue, 50L)

        // 即座に確認（まだ有効）
        val immediateWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(actualRarityWeight * 2.0, immediateWeight, 0.001)

        // 期限を過ぎてから確認
        Thread.sleep(100L)

        val expiredWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertTrue(
            expiredWeight <= actualRarityWeight + 0.001,
            "期限切れ後は修正値が無効になるべき: expected=$actualRarityWeight, actual=$expiredWeight",
        )
    }
}
