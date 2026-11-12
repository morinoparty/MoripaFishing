package party.morino.moripafishing.core.fish

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockbukkit.mockbukkit.MockBukkitExtension
import party.morino.moripafishing.api.model.fish.FishItemData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.core.world.WarningRecorder
import party.morino.moripafishing.testing.FakeRarityManager
import party.morino.moripafishing.testing.fish
import party.morino.moripafishing.testing.rarity
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

@ExtendWith(MockBukkitExtension::class)
class FishManagerImplTest {
    @TempDir
    lateinit var directory: File

    private val warnings = WarningRecorder()
    private val logger =
        Logger.getAnonymousLogger().apply {
            level = Level.WARNING
            addHandler(warnings)
        }
    private val rarities = FakeRarityManager(rarity("common"))
    private lateinit var manager: FishManagerImpl

    @BeforeEach
    fun setUp() {
        manager = FishManagerImpl(directory, rarities, logger)
    }

    private fun write(
        path: String,
        text: String,
    ) = directory.resolve(path).apply { parentFile.mkdirs() }.writeText(text)

    private fun fishJson(
        id: String,
        rarity: String = "common",
        material: String = "COD",
        extra: String = "",
    ) = """{"${'$'}schema": "https://fishing.plugin.morino.party/schema/fish/v1.json", "id": "$id", "rarity": "$rarity",
        |"displayName": {"ja_JP": "$id"}, "item": {"material": "$material"}, "size": {"min": 1.0, "max": 2.0}$extra}
        """.trimMargin()

    @Test
    fun `reload loads nested files without moving them`() {
        write("tuna.json", fishJson("tuna"))
        write("other/deep/cod.json", fishJson("cod"))
        manager.reload()
        assertEquals(listOf("cod", "tuna"), manager.getAll().map { it.id.value }.sorted())
        assertTrue(directory.resolve("other/deep/cod.json").exists())
        assertEquals(2, manager.getByRarity(RarityId("common")).size)
    }

    @Test
    fun `an unknown key is skipped with a warning`() {
        write("tuna.json", fishJson("tuna", extra = """, "icon": "x""""))
        manager.reload()
        assertTrue(manager.getAll().isEmpty())
        assertTrue(warnings.messages.single().startsWith("Skipping fish file tuna.json:"), warnings.messages.toString())
    }

    @Test
    fun `a malformed file is skipped while the others load`() {
        write("broken.json", "not json")
        write("tuna.json", fishJson("tuna"))
        manager.reload()
        assertEquals(listOf("tuna"), manager.getAll().map { it.id.value })
    }

    @Test
    fun `invalid definitions are skipped`() {
        write("unknown_rarity.json", fishJson("a", rarity = "missing"))
        write("unknown_material.json", fishJson("b", material = "NOT_A_MATERIAL"))
        write("block_only.json", fishJson("c", material = "WATER"))
        write("bad_formula.json", fishJson("d", extra = """, "worth": "length_rate +""""))
        manager.reload()
        assertTrue(manager.getAll().isEmpty())
        assertEquals(4, warnings.messages.size)
    }

    @Test
    fun `the first file wins for a duplicate id`() {
        write("a.json", fishJson("tuna"))
        write("b.json", fishJson("tuna", material = "SALMON"))
        manager.reload()
        assertEquals(
            "COD",
            manager
                .getAll()
                .single()
                .item.material,
        )
        assertEquals(listOf("Skipping fish file b.json: duplicate id 'tuna'"), warnings.messages)
    }

    @Test
    fun `reload drops removed files`() {
        write("tuna.json", fishJson("tuna"))
        manager.reload()
        directory.resolve("tuna.json").delete()
        manager.reload()
        assertTrue(manager.getAll().isEmpty())
    }

    @Test
    fun `register throws on an invalid fish`() {
        assertThrows<IllegalArgumentException> { manager.register(fish("a", "missing")) }
        assertThrows<IllegalArgumentException> { manager.register(fish("b", "common", item = FishItemData("NOPE"))) }
        assertThrows<IllegalArgumentException> { manager.register(fish("c", "common", worth = "foo")) }
        manager.register(fish("d", "common"))
        assertEquals(listOf("d"), manager.getAll().map { it.id.value })
    }
}
