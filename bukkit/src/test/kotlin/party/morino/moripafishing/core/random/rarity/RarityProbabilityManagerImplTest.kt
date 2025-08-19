package party.morino.moripafishing.core.random.rarity

import org.junit.jupiter.api.Assertions.assertEquals
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
import party.morino.moripafishing.mocks.angler.AnglerMock
import party.morino.moripafishing.mocks.world.FishingWorldMock

/**
 * RarityProbabilityManagerImplのメインテストクラス
 * レアリティの確率修正機能の基本動作をテストする
 */
@ExtendWith(MoripaFishingTest::class)
class RarityProbabilityManagerImplTest : KoinTest {
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
    @DisplayName("RarityProbabilityManager Basic No.1: レアリティの確率修正が正しく適用される")
    fun testRarityModificationCorrectlyApplied() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")

        // レアリティ修正値を適用
        rarityProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, applyValue)

        // 実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証（実際の基本重み × 2.0）
        assertEquals(actualRarityWeight * 2.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("RarityProbabilityManager Basic No.2: 基本的なレアリティ修正値システムが動作する")
    fun testBasicRarityModifierSystem() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        // レアリティ修正値を適用
        val rarityApplyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "テスト用レアリティ修正")
        rarityProbabilityManager.applyRarityModifierForAngler(anglerId, testRarityId, rarityApplyValue)

        // レアリティ重みを取得
        val modifiedRarityWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：レアリティ修正が適用されている
        assertEquals(actualRarityWeight * 3.0, modifiedRarityWeight, 0.001)

        // クリーンアップ
        probabilityManager.clearAnglerModifiers(anglerId)

        // クリア後の確認
        val clearedRarityWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(actualRarityWeight, clearedRarityWeight, 0.001)
    }
}
