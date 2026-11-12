package party.morino.moripafishing.core.fish

import kotlinx.serialization.json.Json
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.MockBukkitExtension
import party.morino.moripafishing.api.config.ConfigData
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.fish.FishItemData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.core.internationalization.Translations
import party.morino.moripafishing.testing.FakeConfigManager
import party.morino.moripafishing.testing.FakeFishManager
import party.morino.moripafishing.testing.FakeRarityManager
import party.morino.moripafishing.testing.FakeWorldManager
import party.morino.moripafishing.testing.fish
import party.morino.moripafishing.testing.rarity
import java.io.File
import java.time.ZonedDateTime
import java.util.Locale
import java.util.UUID
import java.util.logging.Logger

@ExtendWith(MockBukkitExtension::class)
class FishItemFactoryTest {
    private lateinit var plugin: Plugin
    private lateinit var factory: FishItemFactory
    private val defaultLocale = Locale.getDefault()
    private val caught =
        CaughtFish(
            uniqueId = UUID.randomUUID(),
            fish = FishId("tuna"),
            rarity = RarityId("common"),
            size = 12.345,
            lengthRate = 0.4,
            worth = 10.0,
            angler = AnglerId(UUID.randomUUID()),
            world = FishingWorldId("default"),
            timestamp = ZonedDateTime.parse("2026-11-12T09:30:00+09:00[Asia/Tokyo]"),
        )

    @BeforeEach
    fun setUp() {
        plugin = MockBukkit.createMockPlugin()
        factory = FishItemFactory(plugin, FakeConfigManager(ConfigData(defaultLocale = Locale.JAPAN)))
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    private fun plain(component: net.kyori.adventure.text.Component) = PlainTextComponentSerializer.plainText().serialize(component)

    @Test
    fun `display name and default lore are rendered with the registered translations`(
        @TempDir directory: File,
    ) {
        val tuna = fish("tuna", "common").copy(displayName = mapOf(Locale.JAPAN to "マグロ"))
        val translations =
            Translations(
                directory,
                FakeConfigManager(),
                FakeFishManager(tuna),
                FakeRarityManager(rarity("common").copy(displayName = mapOf(Locale.JAPAN to "コモン"))),
                FakeWorldManager(),
                Logger.getAnonymousLogger(),
            )
        translations.load()
        try {
            val item = factory.create(caught, tuna, "Steve", Locale.JAPAN)
            assertEquals("マグロ", plain(checkNotNull(item.itemMeta.displayName())))
            assertEquals(
                listOf("レアリティ: コモン", "サイズ: 12.35 cm", "釣った人: Steve"),
                checkNotNull(item.lore()).map(::plain),
            )
        } finally {
            translations.close()
        }
    }

    @Test
    fun `the PDC holds the caught fish JSON`() {
        val item = factory.create(caught, fish("tuna", "common"), "Steve", Locale.JAPAN)
        val json = item.itemMeta.persistentDataContainer.get(NamespacedKey(plugin, "caught_fish"), PersistentDataType.STRING)
        assertEquals(caught, Json.decodeFromString(CaughtFish.serializer(), checkNotNull(json)))
    }

    @Test
    fun `custom model data floats are applied`() {
        val item = factory.create(caught, fish("tuna", "common", item = FishItemData("COD", listOf(1.5f, 2f))), "Steve", Locale.JAPAN)
        assertEquals(listOf(1.5f, 2f), item.itemMeta.customModelDataComponent.floats)
    }

    @Test
    fun `the skull profile carries the textures property`() {
        val texture = "dGV4dHVyZQ=="
        val item =
            factory.create(
                caught,
                fish("head", "common", item = FishItemData("PLAYER_HEAD", skullTexture = texture)),
                "Steve",
                Locale.JAPAN,
            )
        val profile = checkNotNull((item.itemMeta as SkullMeta).playerProfile)
        assertEquals(texture, profile.properties.single { it.name == "textures" }.value)
    }

    @Test
    fun `lore falls back to the default locale and formats size with Locale ROOT`() {
        Locale.setDefault(Locale.GERMANY)
        val data = fish("tuna", "common", lore = mapOf(Locale.JAPAN to listOf("<size>cm by <angler>")))
        val lore = checkNotNull(factory.create(caught, data, "Steve", Locale.US).lore())
        assertEquals(4, lore.size)
        assertEquals("12.35cm by Steve", plain(lore.last()))
    }

    @Test
    fun `lore falls back to the language-only locale before the default locale`() {
        val data =
            fish(
                "tuna",
                "common",
                lore = mapOf(Locale.ENGLISH to listOf("english"), Locale.JAPAN to listOf("japanese")),
            )
        val lore = checkNotNull(factory.create(caught, data, "Steve", Locale.US).lore())
        assertEquals("english", plain(lore.last()))
    }
}
