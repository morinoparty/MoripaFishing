package party.morino.moripafishing.core.fishing.rod

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.core.fishing.FishingManager
import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.api.model.rod.RodPresetId

/**
 * RodPresetManagerImplのテストクラス
 * rodプリセットの読み込み、追加、管理機能をテストする
 */
@ExtendWith(MoripaFishingTest::class)
class RodPresetManagerImplTest : KoinTest {
    private val fishingManager: FishingManager by inject()
    private val rodPresetManager get() = fishingManager.getRodPresetManager()

    /**
     * プリセットの読み込みテスト
     * テスト用のプリセットファイルが正しく読み込まれているか確認する
     * ./gradlew test --tests "party.morino.moripafishing.core.rod.RodPresetManagerImplTest.loadPresets"
     */
    @Test
    @DisplayName("プリセットの読み込みテスト")
    fun loadPresets() =
        runBlocking {
            // プリセット一覧を取得
            val presetIds = rodPresetManager.getAllPresetIds()
            println("利用可能なプリセット: $presetIds")

            // テスト用プリセットが読み込まれているか確認
            assertTrue(presetIds.contains(RodPresetId("test_beginner")), "test_beginnerプリセットが読み込まれていない")
            assertTrue(presetIds.contains(RodPresetId("test_master")), "test_masterプリセットが読み込まれていない")

            // プリセットが2つ以上読み込まれているか確認
            assertTrue(presetIds.size >= 2, "プリセットが十分に読み込まれていない")
        }

    /**
     * 特定のプリセット取得テスト
     * 指定したプリセット名で正しい設定が取得できるか確認する
     */
    @Test
    @DisplayName("特定のプリセット取得テスト")
    fun getSpecificPreset() =
        runBlocking {
            // test_beginnerプリセットを取得
            val beginnerPreset = rodPresetManager.getPreset(RodPresetId("test_beginner"))
            assertNotNull(beginnerPreset, "test_beginnerプリセットが取得できない")

            beginnerPreset?.let { preset ->
                assertEquals(RodPresetId("test_beginner"), preset.rodType, "rodTypeが一致しない")
                assertEquals(1.2, preset.waitTimeMultiplier, 0.01, "waitTimeMultiplierが一致しない")
                assertEquals(false, preset.weatherImmunity, "weatherImmunityが一致しない")
                assertEquals(0.9, preset.fishingWorldBonuses["default"] ?: 0.0, 0.01, "defaultボーナスが一致しない")
            }

            // test_masterプリセットを取得
            val masterPreset = rodPresetManager.getPreset(RodPresetId("test_master"))
            assertNotNull(masterPreset, "test_masterプリセットが取得できない")

            masterPreset?.let { preset ->
                assertEquals(RodPresetId("test_master"), preset.rodType, "rodTypeが一致しない")
                assertEquals(0.8, preset.waitTimeMultiplier, 0.01, "waitTimeMultiplierが一致しない")
                assertEquals(true, preset.weatherImmunity, "weatherImmunityが一致しない")
                assertEquals(1.1, preset.fishingWorldBonuses["default"] ?: 0.0, 0.01, "defaultボーナスが一致しない")
            }

            println("test_beginner設定: $beginnerPreset")
            println("test_master設定: $masterPreset")
        }

    /**
     * プリセット存在確認テスト
     * hasPresetメソッドが正しく動作するか確認する
     */
    @Test
    @DisplayName("プリセット存在確認テスト")
    fun hasPresetTest() =
        runBlocking {
            // 存在するプリセット
            assertTrue(rodPresetManager.hasPreset(RodPresetId("test_beginner")), "test_beginnerが存在しない")
            assertTrue(rodPresetManager.hasPreset(RodPresetId("test_master")), "test_masterが存在しない")

            // 大文字小文字の確認
            assertTrue(rodPresetManager.hasPreset(RodPresetId("TEST_BEGINNER")), "大文字小文字変換が機能しない")

            // 存在しないプリセット
            assertFalse(rodPresetManager.hasPreset(RodPresetId("nonexistent")), "存在しないプリセットでtrueが返された")
        }

    /**
     * プリセット追加テスト
     * addPresetメソッドで新しいプリセットが追加できるか確認する
     */
    @Test
    @DisplayName("プリセット追加テスト")
    fun addPresetTest() =
        runBlocking {
            // 新しいプリセット設定を作成
            val newPresetConfig =
                RodConfiguration(
                    rodType = RodPresetId("test_custom"),
                    waitTimeMultiplier = 0.5,
                    bonusEffects = emptyList(),
                    weatherImmunity = true,
                    fishingWorldBonuses = mapOf("default" to 1.5),
                    displayNameKey = "rod.test_custom.name",
                    loreKeys = listOf("rod.test_custom.lore.1", "rod.test_custom.lore.2"),
                )

            // プリセットを追加
            val addResult = rodPresetManager.addPreset(RodPresetId("test_custom"), newPresetConfig)
            assertTrue(addResult, "プリセットの追加に失敗")

            // 追加されたプリセットが存在するか確認
            assertTrue(rodPresetManager.hasPreset(RodPresetId("test_custom")), "追加したプリセットが存在しない")

            // 追加されたプリセットの内容を確認
            val addedPreset = rodPresetManager.getPreset(RodPresetId("test_custom"))
            assertNotNull(addedPreset, "追加したプリセットが取得できない")

            addedPreset?.let { preset ->
                assertEquals(RodPresetId("test_custom"), preset.rodType, "追加したプリセットのrodTypeが一致しない")
                assertEquals(0.5, preset.waitTimeMultiplier, 0.01, "追加したプリセットのwaitTimeMultiplierが一致しない")
                assertEquals(true, preset.weatherImmunity, "追加したプリセットのweatherImmunityが一致しない")
                assertEquals(1.5, preset.fishingWorldBonuses["default"] ?: 0.0, 0.01, "追加したプリセットのdefaultボーナスが一致しない")
            }

            // プリセット一覧に追加されているか確認
            val updatedPresetIds = rodPresetManager.getAllPresetIds()
            assertTrue(updatedPresetIds.contains(RodPresetId("test_custom")), "プリセット一覧に追加されていない")

            println("追加したプリセット: $addedPreset")
            println("更新されたプリセット一覧: $updatedPresetIds")
        }

    /**
     * プリセット再読み込みテスト
     * reloadPresetsメソッドが正しく動作するか確認する
     */
    @Test
    @DisplayName("プリセット再読み込みテスト")
    fun reloadPresetsTest() =
        runBlocking {
            // 初期状態のプリセット数を取得
            val initialPresets = rodPresetManager.getAllPresetIds()
            val initialCount = initialPresets.size

            // プリセットを再読み込み
            rodPresetManager.reloadPresets()

            // 再読み込み後のプリセット数を確認
            val reloadedPresets = rodPresetManager.getAllPresetIds()
            val reloadedCount = reloadedPresets.size

            // プリセット数が維持されているか確認
            assertEquals(initialCount, reloadedCount, "再読み込み後にプリセット数が変わった")

            // 基本プリセットが存在するか確認
            assertTrue(reloadedPresets.contains(RodPresetId("test_beginner")), "再読み込み後にtest_beginnerが存在しない")
            assertTrue(reloadedPresets.contains(RodPresetId("test_master")), "再読み込み後にtest_masterが存在しない")

            println("初期プリセット数: $initialCount")
            println("再読み込み後プリセット数: $reloadedCount")
            println("再読み込み後プリセット一覧: $reloadedPresets")
        }

    /**
     * 存在しないプリセット取得テスト
     * 存在しないプリセットに対してnullが返されるか確認する
     */
    @Test
    @DisplayName("存在しないプリセット取得テスト")
    fun getNonexistentPresetTest() =
        runBlocking {
            // 存在しないプリセットを取得
            val nonexistentPreset = rodPresetManager.getPreset(RodPresetId("definitely_does_not_exist"))

            // nullが返されることを確認
            assertNull(nonexistentPreset, "存在しないプリセットでnull以外が返された")

            println("存在しないプリセットの取得結果: $nonexistentPreset")
        }
}
