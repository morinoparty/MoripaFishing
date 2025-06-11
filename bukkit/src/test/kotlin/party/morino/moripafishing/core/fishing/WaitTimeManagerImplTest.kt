package party.morino.moripafishing.core.fishing

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.fishing.FishingManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.api.model.world.Spot
import party.morino.moripafishing.mocks.angler.AnglerMock
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * WaitTimeManagerImplのテストクラス
 * ./gradlew test --tests "party.morino.moripafishing.core.fishing.WaitTimeManagerImplTest.applyForMultipleAnglers"
 * 待機時間の計算、適用値の管理、期限切れの処理等をテストする
 */
@ExtendWith(MoripaFishingTest::class)
class WaitTimeManagerImplTest : KoinTest {
    private val fishingManager: FishingManager by inject()
    private val waitTimeManager by lazy { fishingManager.getWaitTimeManager() }

    // テスト用のデータ
    private lateinit var testAnglerId: AnglerId
    private lateinit var testWorldId: FishingWorldId
    private lateinit var testSpot: Spot
    private lateinit var testAngler: AnglerMock

    @BeforeEach
    fun setUp() {
        // 前のテストの影響をクリア
        val manager = waitTimeManager as? WaitTimeManagerImpl
        manager?.clearAllEffects()

        // テスト用のIDを作成
        testAnglerId = AnglerId(UUID.randomUUID())
        testWorldId = FishingWorldId("test-world")

        // テスト用のスポットを作成
        val testLocation = Location(testWorldId, 100.0, 64.0, 100.0, 0.0, 0.0)
        testSpot = Spot(testLocation, 50.0)

        // テスト用のAnglerMockを作成
        testAngler = AnglerMock(testAnglerId)
    }

    /**
     * ベース待機時間のテスト - 何も適用されていない状態での基本動作
     */
    @Test
    @DisplayName("WaitTimeManager No.1: 基本的な待機時間を取得できる")
    fun getBaseWaitTime() {
        // 何も適用値が設定されていない状態でのテスト
        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // デフォルト設定(5秒〜30秒)が適用されることを確認
        assertEquals(5.0, waitTime.first) // 最小時間
        assertEquals(30.0, waitTime.second) // 最大時間
    }

    /**
     * Angler単位での適用値テスト - ADD効果
     */
    @Test
    @DisplayName("WaitTimeManager No.2: Angler単位でADD効果を適用できる")
    fun applyForAnglerAddEffect() {
        // 5秒減少する効果を適用
        val addEffect = ApplyValue(ApplyType.ADD, -5.0, "seconds")
        waitTimeManager.applyForAngler(testAnglerId, addEffect, null)

        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // ベース(5-30秒)から5秒減少(0-25秒)、絶対最小値0.5が適用
        assertEquals(0.5, waitTime.first) // max(0.5, 5 - 5) = 0.5
        assertEquals(25.0, waitTime.second) // 30 - 5 = 25
    }

    /**
     * Angler単位での適用値テスト - MULTIPLY効果
     */
    @Test
    @DisplayName("WaitTimeManager No.3: Angler単位でMULTIPLY効果を適用できる")
    fun applyForAnglerMultiplyEffect() {
        // 0.5倍にする効果を適用
        val multiplyEffect = ApplyValue(ApplyType.MULTIPLY, 0.5, "seconds")
        waitTimeManager.applyForAngler(testAnglerId, multiplyEffect, null)

        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // ベース(5-30秒)が0.5倍(2.5-15秒)になる
        assertEquals(2.5, waitTime.first) // 5 * 0.5 = 2.5
        assertEquals(15.0, waitTime.second) // 30 * 0.5 = 15
    }

    /**
     * Angler単位での適用値テスト - CONSTANT効果
     */
    @Test
    @DisplayName("WaitTimeManager No.4: Angler単位でCONSTANT効果を適用できる")
    fun applyForAnglerConstantEffect() {
        // 固定3秒にする効果を適用
        val constantEffect = ApplyValue(ApplyType.CONSTANT, 3.0, "seconds")
        waitTimeManager.applyForAngler(testAnglerId, constantEffect, null)

        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // 固定3秒
        assertEquals(3.0, waitTime.first)
        assertEquals(3.0, waitTime.second)
    }

    /**
     * World単位での適用値テスト
     */
    @Test
    @DisplayName("WaitTimeManager No.5: World単位で適用値を設定できる")
    fun applyForWorld() {
        // ワールド全体に2倍効果を適用
        val worldEffect = ApplyValue(ApplyType.MULTIPLY, 2.0, "seconds")
        waitTimeManager.applyForWorld(testWorldId, worldEffect, null)

        // テスト用Anglerがそのワールドにいるように設定
        testAngler.setTestWorld(testWorldId)

        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // ベース(5-30秒)が2倍(10-60秒)
        assertEquals(10.0, waitTime.first) // 5 * 2 = 10
        assertEquals(60.0, waitTime.second) // 30 * 2 = 60
    }

    /**
     * Spot単位での適用値テスト
     */
    @Test
    @DisplayName("WaitTimeManager No.6: Spot単位で適用値を設定できる")
    fun applyForSpot() {
        // スポットに-10秒効果を適用
        val spotEffect = ApplyValue(ApplyType.ADD, -10.0, "seconds")
        waitTimeManager.applyForSpot(testSpot, spotEffect, null)

        // テスト用Anglerがそのスポットにいるように設定
        testAngler.setTestLocation(testSpot.location)

        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // ベース(5-30秒)から10秒減少、絶対最小値0.5が適用
        assertEquals(0.5, waitTime.first) // max(0.5, 5 - 10) = 0.5
        assertEquals(20.0, waitTime.second) // 30 - 10 = 20
    }

    /**
     * 複数の適用値の組み合わせテスト
     */
    @Test
    @DisplayName("WaitTimeManager No.7: 複数の適用値を組み合わせて適用できる")
    fun applyForMultiple() {
        // World: 2倍
        val worldEffect = ApplyValue(ApplyType.MULTIPLY, 2.0, "seconds")
        waitTimeManager.applyForWorld(testWorldId, worldEffect, null)

        // Angler: -5秒
        val anglerEffect = ApplyValue(ApplyType.ADD, -5.0, "seconds")
        waitTimeManager.applyForAngler(testAnglerId, anglerEffect, null)

        // Spot: 0.5倍
        val spotEffect = ApplyValue(ApplyType.MULTIPLY, 0.5, "seconds")
        waitTimeManager.applyForSpot(testSpot, spotEffect, null)

        // Anglerがワールドとスポットにいるように設定
        testAngler.setTestWorld(testWorldId)
        testAngler.setTestLocation(testSpot.location)

        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // 計算順序: World → Angler → Spot
        // ベース: 5-30
        // World適用後: (5*2)-(30*2) = 10-60
        // Angler適用後: (10-5)-(60-5) = 5-55
        // Spot適用後: (5*0.5)-(55*0.5) = 2.5-27.5
        assertEquals(2.5, waitTime.first)
        assertEquals(27.5, waitTime.second)
    }

    /**
     * 期限切れテスト
     */
    @Test
    @DisplayName("WaitTimeManager No.8: 期限切れの適用値は自動的に削除される")
    fun applyForLongLived() {
        // 即座に期限切れになる適用値を設定（1ミリ秒後）
        val shortLivedEffect = ApplyValue(ApplyType.ADD, -10.0, "seconds")
        waitTimeManager.applyForAngler(testAnglerId, shortLivedEffect, 1L)

        // 期限切れになるまで少し待つ
        Thread.sleep(10) // 10ミリ秒待機

        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // 期限切れの効果は適用されず、ベース値になる
        assertEquals(5.0, waitTime.first)
        assertEquals(30.0, waitTime.second)
    }

    /**
     * 期限ありの適用値テスト（まだ有効）
     */
    @Test
    @DisplayName("WaitTimeManager No.9: 期限内の適用値は正常に適用される")
    fun applyForShortLived() {
        // 1時間後に期限切れになる適用値を設定
        val longLivedEffect = ApplyValue(ApplyType.ADD, -3.0, "seconds")
        waitTimeManager.applyForAngler(testAnglerId, longLivedEffect, 3600000L) // 1時間

        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // 効果が適用される
        assertEquals(2.0, waitTime.first) // 5 - 3 = 2
        assertEquals(27.0, waitTime.second) // 30 - 3 = 27
    }

    /**
     * 絶対制限値のテスト
     */
    @Test
    @DisplayName("WaitTimeManager No.10: 絶対制限値が正しく適用される")
    fun applyForAbsoluteMin() {
        // 大幅に減少させる効果を適用（絶対最小値を下回る）
        val extremeEffect = ApplyValue(ApplyType.ADD, -100.0, "seconds")
        waitTimeManager.applyForAngler(testAnglerId, extremeEffect, null)

        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // 絶対最小値0.5秒が適用される
        assertEquals(0.5, waitTime.first) // max(0.5, 5-100) = 0.5
        // 最大値も最小値以上になる
        assertTrue(waitTime.second >= waitTime.first)
    }

    /**
     * 大きな値での絶対最大制限テスト
     */
    @Test
    @DisplayName("WaitTimeManager No.11: 絶対最大値が正しく適用される")
    fun applyForAbsoluteMax() {
        // 大幅に増加させる効果を適用（絶対最大値を上回る）
        val extremeEffect = ApplyValue(ApplyType.MULTIPLY, 100.0, "seconds")
        waitTimeManager.applyForAngler(testAnglerId, extremeEffect, null)

        val waitTime = waitTimeManager.getWaitTime(testAngler)

        // 最小値が500.0になるため、最大値も最小値以上になる
        assertEquals(500.0, waitTime.second) // max(500.0, min(300, 30*100))
        // 最小値も最大値以下になる
        assertTrue(waitTime.first <= waitTime.second)
    }

    /**
     * clearAnglerEffectsのテスト
     */
    @Test
    @DisplayName("WaitTimeManager No.12: clearAnglerEffectsでアングラー効果がクリアされる")
    fun clearAnglerEffects() {
        val manager = waitTimeManager as WaitTimeManagerImpl

        // アングラー効果を適用
        val anglerEffect = ApplyValue(ApplyType.ADD, -10.0, "seconds")
        manager.applyForAngler(testAnglerId, anglerEffect, null)

        // 効果が適用されていることを確認
        var waitTime = manager.getWaitTime(testAngler)
        assertEquals(0.5, waitTime.first) // max(0.5, 5 - 10) = 0.5
        assertEquals(20.0, waitTime.second) // 30 - 10 = 20

        // アングラー効果をクリア
        manager.clearAnglerEffects(testAnglerId)

        // 効果がクリアされたことを確認
        waitTime = manager.getWaitTime(testAngler)
        assertEquals(5.0, waitTime.first) // ベース値に戻る
        assertEquals(30.0, waitTime.second) // ベース値に戻る
    }

    /**
     * 複数のアングラーでの独立性テスト
     */
    @Test
    @DisplayName("WaitTimeManager No.13: 複数のアングラーの効果が独立している")
    fun applyForMultipleAnglers() {
        val anotherId = AnglerId(UUID.randomUUID())
        val anotherAngler = AnglerMock(anotherId)

        // 一つ目のアングラーに効果を適用
        val effect1 = ApplyValue(ApplyType.ADD, -5.0, "seconds")
        waitTimeManager.applyForAngler(testAnglerId, effect1, null)

        // 二つ目のアングラーに別の効果を適用
        val effect2 = ApplyValue(ApplyType.MULTIPLY, 2.0, "seconds")
        waitTimeManager.applyForAngler(anotherId, effect2, null)

        // それぞれの効果が独立していることを確認
        val waitTime1 = waitTimeManager.getWaitTime(testAngler)
        val waitTime2 = waitTimeManager.getWaitTime(anotherAngler)

        assertEquals(0.5, waitTime1.first) // max(0.5, 5 - 5) = 0.5
        assertEquals(25.0, waitTime1.second) // 30 - 5 = 25

        assertEquals(10.0, waitTime2.first) // 5 * 2 = 10
        assertEquals(60.0, waitTime2.second) // 30 * 2 = 60
    }

    /**
     * Spotの範囲テスト（同じLocationなら同じSpotとして扱われる）
     */
    @Test
    @DisplayName("WaitTimeManager No.14: 同じLocationのSpotは同じ効果が適用される")
    fun applyForSameLocation() {
        // 異なる半径のSpotを作成（距離判定を使用するので問題なし）
        val spot1 = Spot(testSpot.location, 10.0)

        // Spotに効果を適用
        val spotEffect = ApplyValue(ApplyType.ADD, -3.0, "seconds")
        waitTimeManager.applyForSpot(spot1, spotEffect, null)

        // 同じLocationにいるAnglerは効果を受ける
        testAngler.setTestLocation(testSpot.location)
        val waitTime = waitTimeManager.getWaitTime(testAngler)

        assertEquals(2.0, waitTime.first) // 5 - 3 = 2
        assertEquals(27.0, waitTime.second) // 30 - 3 = 27
    }
}
