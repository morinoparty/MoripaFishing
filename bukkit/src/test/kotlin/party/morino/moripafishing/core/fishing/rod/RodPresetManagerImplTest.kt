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
            val presetNames = rodPresetManager.getAllPresetNames()
            println("利用可能なプリセット: $presetNames")

            // テスト用プリセットが読み込まれているか確認
            assertTrue(presetNames.contains("test_beginner"), "test_beginnerプリセットが読み込まれていない")
            assertTrue(presetNames.contains("test_master"), "test_masterプリセットが読み込まれていない")

            // プリセットが2つ以上読み込まれているか確認
            assertTrue(presetNames.size >= 2, "プリセットが十分に読み込まれていない")
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
            val beginnerPreset = rodPresetManager.getPreset("test_beginner")
            assertNotNull(beginnerPreset, "test_beginnerプリセットが取得できない")

            beginnerPreset?.let { preset ->
                assertEquals("test_beginner", preset.rodType, "rodTypeが一致しない")
                assertEquals(1.2, preset.waitTimeMultiplier, 0.01, "waitTimeMultiplierが一致しない")
                assertEquals(false, preset.weatherImmunity, "weatherImmunityが一致しない")
                assertEquals(0.9, preset.fishingWorldBonuses["default"] ?: 0.0, 0.01, "defaultボーナスが一致しない")
            }

            // test_masterプリセットを取得
            val masterPreset = rodPresetManager.getPreset("test_master")
            assertNotNull(masterPreset, "test_masterプリセットが取得できない")

            masterPreset?.let { preset ->
                assertEquals("test_master", preset.rodType, "rodTypeが一致しない")
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
            assertTrue(rodPresetManager.hasPreset("test_beginner"), "test_beginnerが存在しない")
            assertTrue(rodPresetManager.hasPreset("test_master"), "test_masterが存在しない")

            // 大文字小文字の確認
            assertTrue(rodPresetManager.hasPreset("TEST_BEGINNER"), "大文字小文字変換が機能しない")

            // 存在しないプリセット
            assertFalse(rodPresetManager.hasPreset("nonexistent"), "存在しないプリセットでtrueが返された")
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
                    rodType = "test_custom",
                    waitTimeMultiplier = 0.5,
                    bonusEffects = emptyList(),
                    weatherImmunity = true,
                    fishingWorldBonuses = mapOf("default" to 1.5),
                    displayNameKey = "rod.test_custom.name",
                    loreKeys = listOf("rod.test_custom.lore.1", "rod.test_custom.lore.2"),
                )

            // プリセットを追加
            val addResult = rodPresetManager.addPreset("test_custom", newPresetConfig)
            assertTrue(addResult, "プリセットの追加に失敗")

            // 追加されたプリセットが存在するか確認
            assertTrue(rodPresetManager.hasPreset("test_custom"), "追加したプリセットが存在しない")

            // 追加されたプリセットの内容を確認
            val addedPreset = rodPresetManager.getPreset("test_custom")
            assertNotNull(addedPreset, "追加したプリセットが取得できない")

            addedPreset?.let { preset ->
                assertEquals("test_custom", preset.rodType, "追加したプリセットのrodTypeが一致しない")
                assertEquals(0.5, preset.waitTimeMultiplier, 0.01, "追加したプリセットのwaitTimeMultiplierが一致しない")
                assertEquals(true, preset.weatherImmunity, "追加したプリセットのweatherImmunityが一致しない")
                assertEquals(1.5, preset.fishingWorldBonuses["default"] ?: 0.0, 0.01, "追加したプリセットのdefaultボーナスが一致しない")
            }

            // プリセット一覧に追加されているか確認
            val updatedPresetNames = rodPresetManager.getAllPresetNames()
            assertTrue(updatedPresetNames.contains("test_custom"), "プリセット一覧に追加されていない")

            println("追加したプリセット: $addedPreset")
            println("更新されたプリセット一覧: $updatedPresetNames")
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
            val initialPresets = rodPresetManager.getAllPresetNames()
            val initialCount = initialPresets.size

            // プリセットを再読み込み
            rodPresetManager.reloadPresets()

            // 再読み込み後のプリセット数を確認
            val reloadedPresets = rodPresetManager.getAllPresetNames()
            val reloadedCount = reloadedPresets.size

            // プリセット数が維持されているか確認
            assertEquals(initialCount, reloadedCount, "再読み込み後にプリセット数が変わった")

            // 基本プリセットが存在するか確認
            assertTrue(reloadedPresets.contains("test_beginner"), "再読み込み後にtest_beginnerが存在しない")
            assertTrue(reloadedPresets.contains("test_master"), "再読み込み後にtest_masterが存在しない")

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
            val nonexistentPreset = rodPresetManager.getPreset("definitely_does_not_exist")

            // nullが返されることを確認
            assertNull(nonexistentPreset, "存在しないプリセットでnull以外が返された")

            println("存在しないプリセットの取得結果: $nonexistentPreset")
        }
}
