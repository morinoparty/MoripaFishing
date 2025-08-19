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
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.mocks.angler.AnglerMock
import party.morino.moripafishing.mocks.world.FishingWorldMock

/**
 * FishProbabilityManagerImplのメインテストクラス
 * 魚の確率修正機能の基本動作をテストする
 */
@ExtendWith(MoripaFishingTest::class)
class FishProbabilityManagerImplTest : KoinTest {
    private lateinit var fishProbabilityManager: FishProbabilityManagerImpl
    private lateinit var angler: AnglerMock
    private lateinit var fishingWorld: FishingWorldMock

    // 実際のマネージャーを注入
    private val fishManager: FishManager by inject()
    private val worldManager: WorldManager by inject()

    // 実際のデータを使用
    private lateinit var testFishId: FishId
    private lateinit var testWorldId: FishingWorldId

    // 実際のデータの重み
    private var actualFishWeight: Double = 0.0

    @BeforeEach
    fun setUp() {
        fishProbabilityManager = FishProbabilityManagerImpl()

        // 実際のデータを取得
        testFishId = fishManager.getFish().firstOrNull()?.id ?: FishId("sillago_japonica")
        testWorldId = worldManager.getDefaultWorldId()

        // 基本重みは初期化時に自動読み込みされる
        // 実際のデータから基本重みを取得
        actualFishWeight = fishProbabilityManager.getBaseFishWeight(testFishId)

        // アングラーを設定
        val anglerId = AnglerId(java.util.UUID.randomUUID())
        angler = AnglerMock(anglerId)
        fishingWorld = FishingWorldMock(testWorldId)
        angler.setTestWorld(fishingWorld)
        angler.setTestLocation(Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0))
    }

    @Test
    @DisplayName("FishProbabilityManager Basic No.1: 魚の確率修正が正しく適用される")
    fun testFishModificationCorrectlyApplied() {
        // 準備
        val anglerId = angler.getAnglerUniqueId()

        val applyValue = ApplyValue(ApplyType.ADD, 5.0, "")

        // 魚修正値を適用
        fishProbabilityManager.applyFishModifierForAngler(anglerId, testFishId, applyValue)

        // 実行
        val modifiedWeight = fishProbabilityManager.getModifiedFishWeight(angler, testFishId)

        // 検証（実際の基本重み + 5.0）
        assertEquals(actualFishWeight + 5.0, modifiedWeight, 0.001)
    }
}
