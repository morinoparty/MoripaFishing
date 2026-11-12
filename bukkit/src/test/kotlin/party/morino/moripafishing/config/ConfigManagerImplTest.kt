package party.morino.moripafishing.config

import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import party.morino.moripafishing.api.config.ConfigData
import java.io.File
import java.util.Locale

class ConfigManagerImplTest {
    @TempDir
    lateinit var directory: File

    private val file get() = directory.resolve("config.json")

    @Test
    fun `generates the default file`() {
        val manager = ConfigManagerImpl(file)
        manager.reload()
        assertTrue(file.exists())
        assertEquals(ConfigData(), manager.getConfig())
        assertEquals(ConfigData(), ConfigJson.decodeConfig(ConfigData.serializer(), file.readText()))
    }

    @Test
    fun `a schema key is ignored`() {
        file.writeText("""{"${'$'}schema": "https://example.com/schema.json", "defaultLocale": "en_US"}""")
        val manager = ConfigManagerImpl(file)
        manager.reload()
        assertEquals(Locale.US, manager.getConfig().defaultLocale)
    }

    @Test
    fun `an unknown key throws`() {
        file.writeText("""{"unknown": true}""")
        assertThrows<SerializationException> { ConfigManagerImpl(file).reload() }
    }

    @Test
    fun `a failed reload keeps the previous config`() {
        file.writeText("""{"defaultLocale": "en_US"}""")
        val manager = ConfigManagerImpl(file)
        manager.reload()
        file.writeText("""{"world": {"refreshIntervalSeconds": 0}}""")
        assertThrows<IllegalArgumentException> { manager.reload() }
        assertEquals(Locale.US, manager.getConfig().defaultLocale)
    }
}
