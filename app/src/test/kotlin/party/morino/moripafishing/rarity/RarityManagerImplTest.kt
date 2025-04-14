package party.morino.moripafishing.rarity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.rarity.RarityManager
import party.morino.moripafishing.api.config.PluginDirectory

@ExtendWith(MoripaFishingTest::class)
class RarityManagerImplTest : KoinTest {
    private val rarityManager: RarityManager by inject()
    /**
     * レアリティの読み込みテスト
     * レアリティの設定ファイルを読み込んで、正しく登録されているか確認する
     */
    @Test
    @DisplayName("レアリティの読み込みテスト")
    fun loadRarities() {
        val rarities = rarityManager.getRarities()
        println(rarities.joinToString("\n"))
    }
} 