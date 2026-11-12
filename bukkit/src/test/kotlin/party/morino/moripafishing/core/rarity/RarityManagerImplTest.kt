package party.morino.moripafishing.core.rarity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.core.world.WarningRecorder
import party.morino.moripafishing.testing.rarity
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

class RarityManagerImplTest {
    @TempDir
    lateinit var directory: File

    private val warnings = WarningRecorder()
    private val logger =
        Logger.getAnonymousLogger().apply {
            level = Level.WARNING
            addHandler(warnings)
        }
    private lateinit var manager: RarityManagerImpl

    @BeforeEach
    fun setUp() {
        manager = RarityManagerImpl(directory, logger)
    }

    private fun write(
        path: String,
        text: String,
    ) = directory.resolve(path).apply { parentFile.mkdirs() }.writeText(text)

    private fun rarityJson(
        id: String,
        extra: String = "",
    ) = """{"${'$'}schema": "https://fishing.plugin.morino.party/schema/rarity/v1.json", "id": "$id",
        |"displayName": {"ja_JP": "$id"}, "weight": 1.0, "worth": "length_rate * 2"$extra}
        """.trimMargin()

    @Test
    fun `construction does no I-O and reload loads nested files without moving them`() {
        write("common.json", rarityJson("common"))
        write("nested/deep/rare.json", rarityJson("rare"))
        assertTrue(manager.getAll().isEmpty())
        manager.reload()
        assertEquals(listOf("common", "rare"), manager.getAll().map { it.id.value })
        assertTrue(directory.resolve("nested/deep/rare.json").exists())
    }

    @Test
    fun `an unknown key is skipped with a warning`() {
        write("common.json", rarityJson("common", """, "color": "#ffffff""""))
        manager.reload()
        assertTrue(manager.getAll().isEmpty())
        assertTrue(warnings.messages.single().startsWith("Skipping rarity file common.json:"), warnings.messages.toString())
    }

    @Test
    fun `a malformed file is skipped while the others load`() {
        write("broken.json", "{")
        write("common.json", rarityJson("common"))
        manager.reload()
        assertEquals(listOf("common"), manager.getAll().map { it.id.value })
        assertEquals(1, warnings.messages.size)
    }

    @Test
    fun `a bad formula is skipped`() {
        write("common.json", rarityJson("common").replace("length_rate * 2", "size * 2"))
        manager.reload()
        assertTrue(manager.getAll().isEmpty())
    }

    @Test
    fun `the first file wins for a duplicate id`() {
        write("a.json", rarityJson("common"))
        write("b.json", rarityJson("common").replace("\"weight\": 1.0", "\"weight\": 9.0"))
        manager.reload()
        assertEquals(1.0, manager.get(RarityId("common"))?.weight)
        assertEquals(listOf("Skipping rarity file b.json: duplicate id 'common'"), warnings.messages)
    }

    @Test
    fun `reload drops removed files and registered entries`() {
        write("common.json", rarityJson("common"))
        manager.reload()
        manager.register(rarity("api"))
        directory.resolve("common.json").delete()
        manager.reload()
        assertTrue(manager.getAll().isEmpty())
    }

    @Test
    fun `register validates and replaces`() {
        assertThrows<IllegalArgumentException> { manager.register(rarity("bad", worth = "(")) }
        manager.register(rarity("common", weight = 1.0))
        manager.register(rarity("common", weight = 2.0))
        assertEquals(2.0, manager.get(RarityId("common"))?.weight)
        assertTrue(manager.unregister(RarityId("common")))
        assertNull(manager.get(RarityId("common")))
    }
}
