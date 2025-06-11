package party.morino.moripafishing.mocks.angler

import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.mocks.world.FishingWorldMock
import java.util.UUID

/**
 * テスト用のAnglerモッククラス
 * テストで様々な状況をシミュレートするため、状態を自由に設定できる
 */
class AnglerMock(
    private val anglerId: AnglerId,
    private val minecraftUuid: UUID = anglerId.uuid,
    private val name: String = "TestAngler",
) : Angler {
    // テスト用の状態管理
    private var testWorld: FishingWorld? = null
    private var testLocation: Location? = null
    private var testRodConfiguration: RodConfiguration? = null

    override fun getAnglerUniqueId(): AnglerId {
        return anglerId
    }

    override fun getMinecraftUniqueId(): UUID {
        return minecraftUuid
    }

    override fun getName(): String {
        return name
    }

    override fun getWorld(): FishingWorld? {
        return testWorld
    }

    override fun getLocation(): Location? {
        return testLocation
    }

    override fun getCurrentRodConfiguration(): RodConfiguration? {
        return testRodConfiguration
    }

    /**
     * テスト用: ワールドを設定する
     * @param worldId ワールドID、nullの場合はワールドなし状態
     */
    fun setTestWorld(worldId: FishingWorldId?) {
        testWorld = worldId?.let { FishingWorldMock(it) }
    }

    /**
     * テスト用: FishingWorldオブジェクトを直接設定する
     * @param world 設定するワールド
     */
    fun setTestWorld(world: FishingWorld?) {
        testWorld = world
    }

    /**
     * テスト用: 位置を設定する
     * @param location 位置、nullの場合は位置なし状態
     */
    fun setTestLocation(location: Location?) {
        testLocation = location
    }

    /**
     * テスト用: 座標を指定して位置を設定する
     * @param worldId ワールドID
     * @param x X座標
     * @param y Y座標
     * @param z Z座標
     * @param yaw ヨー角（デフォルト0.0）
     * @param pitch ピッチ角（デフォルト0.0）
     */
    fun setTestLocation(
        worldId: FishingWorldId,
        x: Double,
        y: Double,
        z: Double,
        yaw: Double = 0.0,
        pitch: Double = 0.0,
    ) {
        testLocation = Location(worldId, x, y, z, yaw, pitch)
    }

    /**
     * テスト用: ロッド設定を設定する
     * @param rodConfiguration ロッド設定、nullの場合はロッドなし状態
     */
    fun setTestRodConfiguration(rodConfiguration: RodConfiguration?) {
        testRodConfiguration = rodConfiguration
    }

    /**
     * テスト用: オフライン状態をシミュレート（ワールドも位置もnull）
     */
    fun setOffline() {
        testWorld = null
        testLocation = null
        testRodConfiguration = null
    }
}
