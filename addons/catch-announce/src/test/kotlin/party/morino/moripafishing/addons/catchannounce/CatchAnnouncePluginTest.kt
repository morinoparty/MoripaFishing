package party.morino.moripafishing.addons.catchannounce

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class CatchAnnouncePluginTest {
    private lateinit var server: ServerMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    @DisplayName("plugin disables itself when MoripaFishing core is absent")
    fun `disables itself when core plugin is absent`() {
        val plugin = MockBukkit.load(CatchAnnouncePlugin::class.java)
        assertFalse(plugin.isEnabled)
    }
}
