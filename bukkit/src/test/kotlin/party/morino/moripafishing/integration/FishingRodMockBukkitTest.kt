package party.morino.moripafishing.integration

import org.bukkit.Material
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.FishHookMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.random.ProbabilityManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.rod.Hook
import party.morino.moripafishing.api.model.rod.Rod
import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.api.model.world.Spot
import party.morino.moripafishing.core.random.fish.FishProbabilityManagerImpl
import party.morino.moripafishing.mocks.angler.AnglerMock
import party.morino.moripafishing.mocks.world.FishingWorldMock

/**
 * MockBukkitとAnglerMockを使った釣り針位置の統合テスト
 * 釣り針位置によるSpot効果の動作を実際のMinecraft環境でシミュレートして検証する
 */
@ExtendWith(MoripaFishingTest::class)
class FishingRodMockBukkitTest : KoinTest {
    private val server: ServerMock by inject()
    private lateinit var player: PlayerMock
    private lateinit var angler: AnglerMock

    // 実際のマネージャーを注入
    private val probabilityManager: ProbabilityManager by inject()
    private val fishProbabilityManager by lazy { probabilityManager.getFishProbabilityManager() }
    private val rarityProbabilityManager by lazy { probabilityManager.getRarityProbabilityManager() }
    private val worldManager: WorldManager by inject()
    private val rarityManager: RarityManager by inject()

    private lateinit var testRarityId: RarityId
    private lateinit var testWorldId: FishingWorldId

    @BeforeEach
    fun setUp() {
        fishProbabilityManager.cleanupAllFishModifiers()
        rarityProbabilityManager.cleanupAllRarityModifiers()
        // MoripaFishingTestで初期化されたMockBukkitサーバーを取得
        // テスト用プレイヤーを作成
        player = server.addPlayer("TestFisher")

        // プレイヤーに釣竿を渡す
        val fishingRod = ItemStack(Material.FISHING_ROD)
        player.inventory.setItemInMainHand(fishingRod)

        // テストデータを設定
        testRarityId = rarityManager.getRarities().first().id
        testWorldId = worldManager.getDefaultWorldId()

        // AnglerMockを設定
        val anglerId = AnglerId(player.uniqueId)
        angler = AnglerMock(anglerId)
        val fishingWorld = FishingWorldMock(testWorldId)
        angler.setTestWorld(fishingWorld)

        // hookをlocation内に配置
        val hook = Hook(Location(testWorldId, 5.0, 5.0, 5.0, 0.0, 0.0))
        val rod = Rod(configuration = RodConfiguration(), hook)
        angler.setTestRod(rod)
    }

    @Test
    @DisplayName("MockBukkit No.1: 実際に釣竿を振って釣り針位置でSpot効果が適用される")
    fun testActualFishingWithSpotEffect() {
        // プレイヤーを(0, 64, 0)に配置
        val playerLocation = server.worlds[0].getBlockAt(0, 64, 0).location
        player.teleport(playerLocation)
        angler.setTestLocation(Location(testWorldId, 0.0, 64.0, 0.0, 0.0, 0.0))

        // 釣り針が落ちる予定の位置(10, 63, 10)にSpotを設定
        val hookTargetLocation =
            Location(
                worldId = testWorldId,
                x = 10.0,
                y = 63.0,
                z = 10.0,
                yaw = 0.0,
                pitch = 0.0,
            )
        val spot = Spot(hookTargetLocation, 5.0) // 半径5のSpot

        // Spot効果（4倍）を設定
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 4.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 基本重みを取得
        val baseWeight = rarityProbabilityManager.getBaseRarityWeight(testRarityId)

        // 釣り針位置を手動で設定（釣り針を投げた状態をシミュレーション）
        angler.setTestFishingHookLocation(hookTargetLocation)

        // 確率計算を実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：Spot効果が適用されている（4倍）
        assertEquals(baseWeight * 4.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("MockBukkit No.2: 釣り針位置とプレイヤー位置が異なる場合のSpot効果適用")
    fun testSpotEffectWithDifferentHookAndPlayerLocations() {
        // プレイヤーを(0, 64, 0)に配置
        val playerLocation = server.worlds[0].getBlockAt(0, 64, 0).location
        player.teleport(playerLocation)
        angler.setTestLocation(Location(testWorldId, 0.0, 64.0, 0.0, 0.0, 0.0))

        // プレイヤー位置のSpot（効果なし）
        val playerSpotLocation =
            Location(
                worldId = testWorldId,
                x = 0.0,
                y = 64.0,
                z = 0.0,
                yaw = 0.0,
                pitch = 0.0,
            )
        val playerSpot = Spot(playerSpotLocation, 3.0)
        val playerApplyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(playerSpot, testRarityId, playerApplyValue)

        // 釣り針位置のSpot（効果あり）
        val hookSpotLocation =
            Location(
                worldId = testWorldId,
                x = 20.0,
                y = 63.0,
                z = 20.0,
                yaw = 0.0,
                pitch = 0.0,
            )
        val hookSpot = Spot(hookSpotLocation, 5.0)
        val hookApplyValue = ApplyValue(ApplyType.MULTIPLY, 8.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(hookSpot, testRarityId, hookApplyValue)

        // 基本重みを取得
        val baseWeight = rarityProbabilityManager.getBaseRarityWeight(testRarityId)

        // 釣り針位置を設定（プレイヤーから離れた位置）
        angler.setTestFishingHookLocation(hookSpotLocation)

        // 確率計算を実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：釣り針位置のSpot効果が適用されている（8倍、プレイヤー位置の2倍ではない）
        assertEquals(baseWeight * 8.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("MockBukkit No.3: 複数の釣り針位置でのSpot効果の動的変更")
    fun testDynamicSpotEffectWithMultipleHookLocations() {
        // プレイヤーを(0, 64, 0)に配置
        val playerLocation = server.worlds[0].getBlockAt(0, 64, 0).location
        player.teleport(playerLocation)

        angler.setTestLocation(Location(testWorldId, 0.0, 64.0, 0.0, 0.0, 0.0))

        // 3つの異なるSpotを設定
        val spot1Location =
            Location(
                worldId = testWorldId,
                x = 10.0,
                y = 63.0,
                z = 10.0,
                yaw = 0.0,
                pitch = 0.0,
            )
        val spot1 = Spot(spot1Location, 5.0)
        val spot1ApplyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(spot1, testRarityId, spot1ApplyValue)

        val spot2Location =
            Location(
                worldId = testWorldId,
                x = 30.0,
                y = 63.0,
                z = 30.0,
                yaw = 0.0,
                pitch = 0.0,
            )
        val spot2 = Spot(spot2Location, 5.0)
        val spot2ApplyValue = ApplyValue(ApplyType.MULTIPLY, 6.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(spot2, testRarityId, spot2ApplyValue)

        val spot3Location =
            Location(
                worldId = testWorldId,
                x = 50.0,
                y = 63.0,
                z = 50.0,
                yaw = 0.0,
                pitch = 0.0,
            )
        val spot3 = Spot(spot3Location, 5.0)
        val spot3ApplyValue = ApplyValue(ApplyType.MULTIPLY, 9.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(spot3, testRarityId, spot3ApplyValue)

        // 基本重みを取得
        val baseWeight = rarityProbabilityManager.getBaseRarityWeight(testRarityId)

        // 1回目：Spot1の位置で釣り
        angler.setTestFishingHookLocation(spot1Location)
        val modifiedWeight1 = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(baseWeight * 3.0, modifiedWeight1, 0.001)

        // 2回目：Spot2の位置で釣り
        angler.setTestFishingHookLocation(spot2Location)
        val modifiedWeight2 = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(baseWeight * 6.0, modifiedWeight2, 0.001)

        // 3回目：Spot3の位置で釣り
        angler.setTestFishingHookLocation(spot3Location)
        val modifiedWeight3 = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(baseWeight * 9.0, modifiedWeight3, 0.001)
    }

    @Test
    @DisplayName("MockBukkit No.4: 釣り針がSpot範囲外の場合は効果が適用されない")
    fun testNoSpotEffectWhenHookOutsideRange() {
        // プレイヤーを(0, 64, 0)に配置
        val playerLocation = server.worlds[0].getBlockAt(0, 64, 0).location
        player.teleport(playerLocation)

        angler.setTestLocation(Location(testWorldId, 0.0, 64.0, 0.0, 0.0, 0.0))

        // Spotを(10, 63, 10)に設定（半径3の小さなSpot）
        val spotLocation =
            Location(
                worldId = testWorldId,
                x = 10.0,
                y = 63.0,
                z = 10.0,
                yaw = 0.0,
                pitch = 0.0,
            )
        val spot = Spot(spotLocation, 3.0) // 半径3の小さなSpot
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 5.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 釣り針位置（Spot範囲外）
        val hookLocation =
            Location(
                worldId = testWorldId,
                x = 20.0,
                y = 63.0,
                z = 20.0,
                yaw = 0.0,
                pitch = 0.0,
            )

        // 基本重みを取得
        val baseWeight = rarityProbabilityManager.getBaseRarityWeight(testRarityId)

        // 釣り針位置を設定（Spot範囲外）
        angler.setTestFishingHookLocation(hookLocation)

        // 確率計算を実行
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：Spot効果が適用されていない（基本重みのまま）
        assertEquals(baseWeight, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("MockBukkit No.5: 釣り針位置の直接設定とSpot効果検証")
    fun testDirectHookLocationSettingAndSpotEffect() {
        // プレイヤーを(0, 64, 0)に配置
        val playerLocation = server.worlds[0].getBlockAt(0, 64, 0).location
        player.teleport(playerLocation)

        // 直接釣り針位置を設定するテスト
        val hookTestLocation =
            Location(
                worldId = FishingWorldId(player.world.name),
                x = 15.0,
                y = 63.0,
                z = 15.0,
                yaw = 0.0,
                pitch = 0.0,
            )

        // Spotを設定
        val spot = Spot(hookTestLocation, 5.0)
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 10.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // 釣り針位置を直接設定
        angler.setTestFishingHookLocation(hookTestLocation)

        // 設定確認
        val setHookLocation = angler.getCurrentRod()?.getHookLocation()
        assertNotNull(setHookLocation)
        assertEquals(15.0, setHookLocation!!.x, 0.001)
        assertEquals(63.0, setHookLocation.y, 0.001)
        assertEquals(15.0, setHookLocation.z, 0.001)

        // 基本重みとSpot効果適用後の重みを比較
        val baseWeight = rarityProbabilityManager.getBaseRarityWeight(testRarityId)
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：Spot効果が適用されている（10倍）
        assertEquals(baseWeight * 10.0, modifiedWeight, 0.001)
    }

    @Test
    @DisplayName("MockBukkit No.6: FishHookMockを使った実際の釣り針シミュレーション")
    fun testFishingWithFishHookMock() {
        // プレイヤーを(0, 64, 0)に配置
        val playerLocation = server.worlds[0].getBlockAt(0, 64, 0).location
        player.teleport(playerLocation)
        angler.setTestLocation(Location(testWorldId, 0.0, 64.0, 0.0, 0.0, 0.0))

        // 釣り針を(15, 63, 15)に配置してSpot効果を設定
        val hookLocation =
            Location(
                worldId = testWorldId,
                x = 15.0,
                y = 63.0,
                z = 15.0,
                yaw = 0.0,
                pitch = 0.0,
            )

        // Spot効果（3倍）を設定
        val spot = Spot(hookLocation, 5.0)
        val applyValue = ApplyValue(ApplyType.MULTIPLY, 3.0, "")
        rarityProbabilityManager.applyRarityModifierForSpot(spot, testRarityId, applyValue)

        // FishHookMockを作成
        val world = server.worlds[0]
        val fishHook = FishHookMock(server, player.uniqueId)

        // 釣り針の位置を設定
        val bukkitHookLocation = world.getBlockAt(15, 63, 15).location
        fishHook.teleport(bukkitHookLocation)

        // 釣り針の設定
        fishHook.biteChance = 0.8 // 釣れる確率80%
        fishHook.applyLure = true // ルアー効果適用

        // PlayerFishEventをシミュレート（FISHING状態）
        val fishEvent = PlayerFishEvent(player, null, fishHook, PlayerFishEvent.State.FISHING)
        server.pluginManager.callEvent(fishEvent)

        // AnglerMockの釣り針位置を設定
        angler.setTestFishingHookLocation(hookLocation)

        // 確率計算を実行
        val baseWeight = rarityProbabilityManager.getBaseRarityWeight(testRarityId)
        val modifiedWeight = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)

        // 検証：Spot効果が適用されている（3倍）
        assertEquals(baseWeight * 3.0, modifiedWeight, 0.001)

        // FishHookMockの状態確認
        assertEquals(0.8, fishHook.getBiteChance(), 0.001)
        assertEquals(true, fishHook.getApplyLure())
    }

    @Test
    @DisplayName("MockBukkit No.7: 複数の釣り針で異なるSpot効果をテスト")
    fun testMultipleFishHooksWithDifferentSpots() {
        // プレイヤーを(0, 64, 0)に配置
        val playerLocation = server.worlds[0].getBlockAt(0, 64, 0).location
        player.teleport(playerLocation)
        angler.setTestLocation(Location(testWorldId, 0.0, 64.0, 0.0, 0.0, 0.0))

        val world = server.worlds[0]

        // 2つの異なるSpotを設定
        val spot1Location = Location(testWorldId, 10.0, 63.0, 10.0, 0.0, 0.0)
        val spot2Location = Location(testWorldId, 20.0, 63.0, 20.0, 0.0, 0.0)

        val spot1 = Spot(spot1Location, 5.0)
        val spot2 = Spot(spot2Location, 5.0)

        val spot1ApplyValue = ApplyValue(ApplyType.MULTIPLY, 2.0, "")
        val spot2ApplyValue = ApplyValue(ApplyType.MULTIPLY, 4.0, "")

        rarityProbabilityManager.applyRarityModifierForSpot(spot1, testRarityId, spot1ApplyValue)
        rarityProbabilityManager.applyRarityModifierForSpot(spot2, testRarityId, spot2ApplyValue)

        // 基本重みを取得
        val baseWeight = rarityProbabilityManager.getBaseRarityWeight(testRarityId)

        // 1つ目の釣り針：Spot1の位置
        val fishHook1 = FishHookMock(server, player.uniqueId)
        val bukkitHookLocation1 = world.getBlockAt(10, 63, 10).location
        fishHook1.teleport(bukkitHookLocation1)
        fishHook1.setBiteChance(0.5)

        angler.setTestFishingHookLocation(spot1Location)
        val modifiedWeight1 = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(baseWeight * 2.0, modifiedWeight1, 0.001)

        // 2つ目の釣り針：Spot2の位置
        val fishHook2 = FishHookMock(server, player.uniqueId)
        val bukkitHookLocation2 = world.getBlockAt(20, 63, 20).location
        fishHook2.teleport(bukkitHookLocation2)
        fishHook2.setBiteChance(0.7)

        angler.setTestFishingHookLocation(spot2Location)
        val modifiedWeight2 = rarityProbabilityManager.getModifiedRarityWeight(angler, testRarityId)
        assertEquals(baseWeight * 4.0, modifiedWeight2, 0.001)

        // FishHookMockの設定確認
        assertEquals(0.5, fishHook1.getBiteChance(), 0.001)
        assertEquals(0.7, fishHook2.getBiteChance(), 0.001)
    }
}
