package party.morino.moripafishing.addons.catchannounce

import com.charleskorn.kaml.Yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CatchAnnounceConfigTest {
    @Test
    fun `the default config round-trips`() {
        val text = Yaml.default.encodeToString(CatchAnnounceConfig.serializer(), CatchAnnounceConfig())
        assertEquals(CatchAnnounceConfig(), Yaml.default.decodeFromString(CatchAnnounceConfig.serializer(), text))
    }

    @Test
    fun `an unknown key such as the removed enabled is rejected`() {
        assertThrows<IllegalArgumentException> {
            Yaml.default.decodeFromString(CatchAnnounceConfig.serializer(), "enabled: true\n")
        }
    }
}
