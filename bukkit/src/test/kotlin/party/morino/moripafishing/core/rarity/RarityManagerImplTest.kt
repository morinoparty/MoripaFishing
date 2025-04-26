package party.morino.moripafishing.core.rarity

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.core.rarity.RarityManager

@ExtendWith(MoripaFishingTest::class)
class RarityManagerImplTest : KoinTest {
    private val rarityManager: RarityManager by inject()

    /**
     * レアリティの読み込みテスト
     * レアリティの設定ファイルを読み込んで、正しく登録されているか確認する
     * ./gradlew test --tests "party.morino.moripafishing.core.rarity.RarityManagerImplTest.loadRarities"
     */
    @Test
    @DisplayName("レアリティの読み込みテスト")
    fun loadRarities() {
        val rarities = rarityManager.getRarities()
        val sum = rarities.sumOf { it.weight }
        println(rarities.joinToString("\n") { "${it.id.value.padEnd(10)}  ${it.weight}" })
    }
}
