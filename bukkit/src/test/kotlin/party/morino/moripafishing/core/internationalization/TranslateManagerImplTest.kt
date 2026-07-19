package party.morino.moripafishing.core.internationalization

import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import party.morino.moripafishing.MoripaFishingTest
import java.util.Locale

/**
 * TranslateManagerImplのテストクラス
 */
@ExtendWith(MoripaFishingTest::class)
class TranslateManagerImplTest : KoinTest {
    @Test
    @DisplayName("reload rebuilds the translation store without duplicate-key errors")
    fun reloadDoesNotThrow() {
        val manager = TranslateManagerImpl()
        manager.load()
        // 同一ストアへの再登録は IllegalArgumentException になるため、
        // reload はストアを作り直す必要がある (regression: /mf reload)
        assertDoesNotThrow {
            manager.reload()
            manager.reload()
        }
        // リロード後も翻訳が解決できること
        val translated =
            GlobalTranslator.translator().translate(
                Component.translatable("moripa_fishing.world.default.name"),
                Locale.JAPAN,
            )
        assertNotNull(translated)
    }
}
