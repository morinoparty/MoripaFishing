package party.morino.moripafishing.core.internationalization

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockbukkit.mockbukkit.MockBukkitExtension
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.core.fish.FishManagerImpl
import party.morino.moripafishing.core.rarity.RarityManagerImpl
import party.morino.moripafishing.testing.FakeConfigManager
import party.morino.moripafishing.testing.fish
import party.morino.moripafishing.testing.rarity
import java.io.File
import java.util.Locale
import java.util.logging.Logger

@ExtendWith(MockBukkitExtension::class)
class TranslationsTest {
    @TempDir
    lateinit var directory: File

    private lateinit var translations: Translations
    private val worldId = FishingWorldId("default")

    @BeforeEach
    fun setUp() {
        translations = Translations(directory, FakeConfigManager(), Logger.getAnonymousLogger())
        translations.register(worldId.toTranslateKey(), mapOf(Locale.JAPAN to "つりとぴあ"))
        translations.register(FishId("tuna").toTranslateKey(), mapOf(Locale.JAPAN to "tuna"))
        translations.register(RarityId("common").toTranslateKey(), mapOf(Locale.JAPAN to "common"))
    }

    @AfterEach
    fun tearDown() {
        translations.close()
    }

    private fun render(
        key: String,
        locale: Locale,
    ): String = render(Component.translatable(key), locale)

    private fun render(
        component: Component,
        locale: Locale,
    ): String = PlainTextComponentSerializer.plainText().serialize(GlobalTranslator.render(component, locale))

    private fun ownSources() = GlobalTranslator.translator().sources().filter { it.name() == Key.key("moripafishing", "translations") }

    @Test
    fun `copies the bundled files`() {
        translations.load()
        assertTrue(directory.resolve("ja_JP.properties").exists())
        assertTrue(directory.resolve("en_US.properties").exists())
    }

    @Test
    fun `a user override wins and missing keys fall back to the bundled ones`() {
        directory.resolve("en_US.properties").writeText("moripa_fishing.fish.lore.default.size=Length: <size>\n")
        translations.load()
        assertEquals("Length: <size>", render("moripa_fishing.fish.lore.default.size", Locale.US))
        assertEquals("Angler: <angler>", render("moripa_fishing.fish.lore.default.angler", Locale.US))
    }

    @Test
    fun `an extra locale file is loaded`() {
        directory.resolve("fr_FR.properties").writeText("moripa_fishing.fish.lore.default.size=Taille\n")
        translations.load()
        assertEquals("Taille", render("moripa_fishing.fish.lore.default.size", Locale.FRANCE))
    }

    @Test
    fun `fish, rarity and world names resolve and properties win`() {
        directory.resolve("ja_JP.properties").writeText("moripa_fishing.rarity.common.name=ふつう\n")
        translations.load()
        assertEquals("tuna", render(FishId("tuna").toTranslateKey(), Locale.JAPAN))
        assertEquals("ふつう", render(RarityId("common").toTranslateKey(), Locale.JAPAN))
        assertEquals("つりとぴあ", render(worldId.toTranslateKey(), Locale.JAPAN))
    }

    @Test
    fun `loading again replaces the source and close removes it`() {
        translations.load()
        translations.load()
        assertEquals(1, ownSources().size)
        translations.close()
        assertFalse(ownSources().any())
    }

    @Test
    fun `a malformed file is skipped and the others load`() {
        directory.resolve("en_US.properties").writeText("moripa_fishing.fish.lore.default.size=\\uZZZZ\n")
        directory.resolve("ja_JP.properties").writeText("moripa_fishing.fish.lore.default.size=長さ\n")
        translations.load()
        assertEquals(1, ownSources().size)
        assertEquals("長さ", render("moripa_fishing.fish.lore.default.size", Locale.JAPAN))
    }

    @Test
    fun `files whose name is not a locale are skipped`() {
        directory.resolve("fr_fr.properties").writeText("moripa_fishing.fish.lore.default.size=lower\n")
        directory.resolve("messages.properties").writeText("moripa_fishing.fish.lore.default.size=messages\n")
        translations.load()
        assertEquals(1, ownSources().size)
        assertNotEquals("lower", render("moripa_fishing.fish.lore.default.size", Locale.FRANCE))
        assertNotEquals("messages", render("moripa_fishing.fish.lore.default.size", Locale.of("messages")))
    }

    @Test
    fun `a second file for the same locale is skipped`() {
        directory.resolve("he.properties").writeText("moripa_fishing.fish.lore.default.size=he\n")
        directory.resolve("iw.properties").writeText("moripa_fishing.fish.lore.default.size=iw\n")
        translations.load()
        assertEquals("he", render("moripa_fishing.fish.lore.default.size", Locale.of("he")))
    }

    @Test
    fun `a name registered after loading resolves until it is unregistered`() {
        translations.load()
        val event = FishId("event")
        translations.register(event.toTranslateKey(), mapOf(Locale.JAPAN to "イベント魚"))
        assertEquals("イベント魚", render(event.toTranslatable(), Locale.JAPAN))
        translations.register(event.toTranslateKey(), mapOf(Locale.JAPAN to "改名した魚"))
        assertEquals("改名した魚", render(event.toTranslatable(), Locale.JAPAN))
        translations.unregister(event.toTranslateKey())
        assertEquals("event", render(event.toTranslatable(), Locale.JAPAN))
    }

    @Test
    fun `properties keep winning when a name is registered or unregistered later`() {
        directory.resolve("ja_JP.properties").writeText("moripa_fishing.rarity.event.name=限定\n")
        translations.load()
        val key = RarityId("event").toTranslateKey()
        translations.register(key, mapOf(Locale.JAPAN to "イベント", Locale.US to "Event"))
        assertEquals("限定", render(key, Locale.JAPAN))
        assertEquals("Event", render(key, Locale.US))
        translations.unregister(key)
        assertEquals("限定", render(key, Locale.JAPAN))
    }

    @Test
    fun `names unregistered before loading are not registered`() {
        translations.unregister(FishId("tuna").toTranslateKey())
        translations.load()
        assertEquals("tuna", render(FishId("tuna").toTranslatable(), Locale.JAPAN))
    }

    @Test
    fun `a missing key falls back to the id`() {
        translations.load()
        assertEquals("gone", render(FishId("gone").toTranslatable(), Locale.JAPAN))
        assertEquals("gone", render(RarityId("gone").toTranslatable(), Locale.JAPAN))
        assertEquals("gone", render(FishingWorldId("gone").toTranslatable(), Locale.JAPAN))
    }

    @Test
    fun `names of definitions registered through the managers after loading resolve until unregistered`() {
        val logger = Logger.getAnonymousLogger()
        val rarities = RarityManagerImpl(directory.resolve("rarity").apply { mkdirs() }, translations, logger)
        val fishes = FishManagerImpl(directory.resolve("fish").apply { mkdirs() }, rarities, translations, logger)
        rarities.reload()
        fishes.reload()
        translations.load()

        rarities.register(rarity("event").copy(displayName = mapOf(Locale.JAPAN to "限定")))
        fishes.register(fish("festival", "event").copy(displayName = mapOf(Locale.JAPAN to "祭りの魚")))
        assertEquals("祭りの魚", render(FishId("festival").toTranslatable(), Locale.JAPAN))
        assertEquals("限定", render(RarityId("event").toTranslatable(), Locale.JAPAN))

        assertTrue(fishes.unregister(FishId("festival")))
        assertTrue(rarities.unregister(RarityId("event")))
        assertEquals("festival", render(FishId("festival").toTranslatable(), Locale.JAPAN))
        assertEquals("event", render(RarityId("event").toTranslatable(), Locale.JAPAN))
    }
}
