package party.morino.moripafishing.core.internationalization

import org.junit.jupiter.api.Test
import java.util.Locale

class TranslateManagerTest {
    @Test
    fun testLocaleTag() {
        val locale = Locale.JAPAN
        println(locale.toString())
        val localeString = "ja_JP"
        val localeFromString = Locale.forLanguageTag(localeString.replace('_', '-'))
        println(localeFromString.toString())
    }
}
