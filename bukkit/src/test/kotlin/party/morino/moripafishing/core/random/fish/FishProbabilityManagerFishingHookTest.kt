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
import party.morino.moripafishing.api.core.random.ProbabilityManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.rod.Hook
import party.morino.moripafishing.api.model.rod.Rod
import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.api.model.world.Spot
import party.morino.moripafishing.mocks.angler.AnglerMock
import party.morino.moripafishing.mocks.world.FishingWorldMock

/**
 * 釣り針位置による確率修正機能のテストクラス
 * プレイヤー位置と釣り針位置が異なる場合の動作を検証する
 */
@ExtendWith(MoripaFishingTest::class)
class FishProbabilityManagerFishingHookTest : KoinTest {
    private lateinit var angler: AnglerMock
    private lateinit var fishingWorld: FishingWorldMock

    // 実際のマネージャーを注入
    private val fishManager: FishManager by inject()
    private val rarityManager: RarityManager by inject()
    private val probabilityManager: ProbabilityManager by inject()
    private val rarityProbabilityManager by lazy { probabilityManager.getRarityProbabilityManager() }
    private val fishProbabilityManager by lazy { probabilityManager.getFishProbabilityManager() }
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
        rarityProbabilityManager.cleanupAllRarityModifiers()
        fishProbabilityManager.cleanupAllFishModifiers()
        // 実際のデータを取得
        testRarityId = rarityManager.getRarities().firstOrNull()?.id ?: RarityId("common")
        testFishId = fishManager.getFish().firstOrNull()?.id ?: FishId("sillago_japonica")
        testWorldId = worldManager.getDefaultWorldId()

        // 基本重みは初期化時に自動読み込みされる
        // 実際のデータから基本重みを取得
        actualRarityWeight = rarityProbabilityManager.getBaseRarityWeight(testRarityId)
        actualFishWeight = fishProbabilityManager.getBaseFishWeight(testFishId)

        // アングラーを設定
        val anglerId = AnglerId(java.util.UUID.randomUUID())
        angler = AnglerMock(anglerId)
        fishingWorld = FishingWorldMock(testWorldId)
        angler.setTestWorld(fishingWorld)

        // hookをlocation内に配置
        val hook = Hook(Location(testWorldId, 5.0, 5.0, 5.0, 0.0, 0.0))
        val rod = Rod(configuration = RodConfiguration(), hook)
        angler.setTestRod(rod)
    }

    @Test
    @DisplayName("FishingHook No.1: プレイヤー位置と釣り針位置が異なる場合、釣り針位置でSpot効果が適用される")
    fun testSpotEffectAppliedAtFishingHookLocation() {
        // プレイヤーを(0, 0, 0)に配置
        angler.setTestLocation(Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0))

        // 釣り針を(10, 0, 10)に配置（プレイヤーから離れた位置）
        angler.setTestFishingHookLocation(Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0))

        // 釣り針の位置(10, 0, 10)にSpotを設定
        val spotLocation = Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0)
        val spot = Spot(spotLocation, 5.0) // 半径5のSpot

        // Spot効果（3倍）を適用
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 確率計算を実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：釣り針がSpot範囲内にあるので、効果が適用される
        assertEquals(actualRarityWeight * 3.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishingHook No.2: 釣り針がSpot範囲外の場合は効果が適用されない")
    fun testSpotEffectNotAppliedWhenFishingHookOutsideRange() {
        // プレイヤーを(0, 0, 0)に配置
        angler.setTestLocation(Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0))

        // 釣り針を(20, 0, 20)に配置（Spotから離れた位置）
        angler.setTestFishingHookLocation(Location(testWorldId, 20.0, 0.0, 20.0, 0.0, 0.0))

        // Spotを(10, 0, 10)に設定（釣り針から離れた位置）
        val spotLocation = Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0)
        val spot = Spot(spotLocation, 5.0) // 半径5のSpot

        // Spot効果（3倍）を適用
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 確率計算を実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：釣り針がSpot範囲外にあるので、効果が適用されない
        assertEquals(actualRarityWeight, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishingHook No.3: 複数のSpotがある場合、釣り針の位置で適切に選択される")
    fun testMultipleSpotSelectionBasedOnFishingHookLocation() {
        // プレイヤーを(0, 0, 0)に配置
        angler.setTestLocation(Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0))

        // 釣り針を(15, 0, 15)に配置
        angler.setTestFishingHookLocation(Location(testWorldId, 15.0, 0.0, 15.0, 0.0, 0.0))

        // 複数のSpotを設定
        val spot1Location = Location(testWorldId, 5.0, 0.0, 5.0, 0.0, 0.0)
        val spot1 = Spot(spot1Location, 3.0) // 半径3のSpot（釣り針から離れている）

        val spot2Location = Location(testWorldId, 15.0, 0.0, 15.0, 0.0, 0.0)
        val spot2 = Spot(spot2Location, 5.0) // 半径5のSpot（釣り針の位置）

        // Spot1に2倍効果、Spot2に4倍効果を適用
        val applyValue1 = ApplyValue(ApplyType.MULTIPLY, 2.0, "")
        val applyValue2 = ApplyValue(ApplyType.MULTIPLY, 4.0, "")

        rarityProbabilityManager.applyRarityModifierForSpot(spot1, testRarityId, applyValue1)
        rarityProbabilityManager.applyRarityModifierForSpot(spot2, testRarityId, applyValue2)

        // 確率計算を実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：釣り針がSpot2の範囲内にあるので、Spot2の効果（4倍）が適用される
        assertEquals(actualRarityWeight * 4.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishingHook No.4: 釣り針位置がnullの場合はSpot効果が適用されない")
    fun testNoSpotEffectWhenFishingHookLocationIsNull() {
        // プレイヤーを(0, 0, 0)に配置
        angler.setTestLocation(Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0))

        // 釣り針の位置をnullに設定（釣りをしていない状態）
        angler.setTestFishingHookLocation(null)

        // プレイヤーの位置にSpotを設定
        val spotLocation = Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0)
        val spot = Spot(spotLocation, 10.0) // 半径10のSpot

        // Spot効果（5倍）を適用
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 5.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 確率計算を実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：釣り針位置がnullなので、Spot効果は適用されない
        assertEquals(actualRarityWeight, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishingHook No.5: プレイヤー位置にSpotがあっても釣り針位置が優先される")
    fun testFishingHookLocationTakesPriorityOverPlayerLocation() {
        // プレイヤーを(0, 0, 0)に配置
        angler.setTestLocation(Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0))

        // 釣り針を(10, 0, 10)に配置
        angler.setTestFishingHookLocation(Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0))

        // プレイヤーの位置(0, 0, 0)にSpot1を設定
        val playerSpotLocation = Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0)
        val playerSpot = Spot(playerSpotLocation, 5.0) // プレイヤー周辺のSpot

        // 釣り針の位置(10, 0, 10)にSpot2を設定
        val hookSpotLocation = Location(testWorldId, 10.0, 0.0, 10.0, 0.0, 0.0)
        val hookSpot = Spot(hookSpotLocation, 5.0) // 釣り針周辺のSpot

        // プレイヤー位置のSpotに2倍効果、釣り針位置のSpotに6倍効果を適用
        val playerApplyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")
        val hookApplyValue = ApplyValue(ApplyType.MULTIPLY, 6.0, "")

        rarityProbabilityManager.applyRarityModifierForSpot(playerSpot, testRarityId, playerApplyValue)
        rarityProbabilityManager.applyRarityModifierForSpot(hookSpot, testRarityId, hookApplyValue)

        // 確率計算を実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：釣り針の位置が優先されるので、6倍効果が適用される
        assertEquals(actualRarityWeight * 6.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("FishingHook No.6: 魚の確率修正でも釣り針位置が使用される")
    fun testFishProbabilityModificationUsesHookLocation() {
        // プレイヤーを(0, 0, 0)に配置
        angler.setTestLocation(Location(testWorldId, 0.0, 0.0, 0.0, 0.0, 0.0))

        // 釣り針を(12, 0, 12)に配置
        angler.setTestFishingHookLocation(Location(testWorldId, 12.0, 0.0, 12.0, 0.0, 0.0))

        // 釣り針の位置にSpotを設定
        val spotLocation = Location(testWorldId, 12.0, 0.0, 12.0, 0.0, 0.0)
        val spot = Spot(spotLocation, 5.0) // 半径5のSpot

        // 魚のSpot効果（+10.0）を適用
        val applyValue = ApplyValue(ApplyType.ADD, 10.0, "")
        fishProbabilityManager.applyFishModifierForSpot(spot, testFishId, applyValue)

        // 確率計算を実行
        val modifiedWeight = fishProbabilityManager.getModifiedFishWeight(angler, testFishId)

        // 検証：釣り針がSpot範囲内にあるので、魚の効果も適用される
        assertEquals(actualFishWeight + 10.0, modifiedWeight, 0.001)
    }
}
