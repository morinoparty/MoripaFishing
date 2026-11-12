package party.morino.moripafishing.addons.catchannounce

import net.kyori.adventure.text.ComponentIteratorType
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.MockBukkitExtension
import org.mockbukkit.mockbukkit.MockBukkitInject
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import party.morino.moripafishing.api.event.fishing.AnglerFishCaughtResultEvent
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import java.time.ZonedDateTime
import java.util.Locale
import java.util.UUID

@ExtendWith(MockBukkitExtension::class)
class CatchAnnounceListenerTest {
    @MockBukkitInject
    lateinit var server: ServerMock

    private val defaultLocale = Locale.getDefault()
    private val worldId = FishingWorldId("default")
    private val common = RarityData(RarityId("common"), mapOf(Locale.JAPAN to "コモン"), 70.0, "1")
    private val mythic = RarityData(RarityId("mythic"), mapOf(Locale.JAPAN to "マボロシ"), 0.1, "1")

    @AfterEach
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    private fun caught(rarity: RarityId = common.id) =
        CaughtFish(
            uniqueId = UUID.randomUUID(),
            fish = FishId("tuna"),
            rarity = rarity,
            size = 12.345,
            lengthRate = 0.5,
            worth = 1.0,
            angler = AnglerId(UUID.randomUUID()),
            world = worldId,
            timestamp = ZonedDateTime.now(),
        )

    private fun catchWith(
        config: CatchAnnounceConfig,
        api: FakeMoripaFishingAPI = FakeMoripaFishingAPI(listOf(common, mythic)),
        fish: CaughtFish = caught(),
    ) {
        val event = AnglerFishCaughtResultEvent(FakeAngler("Steve", worldId), fish)
        CatchAnnounceListener(api, server, config).onAnglerFishCaught(event)
    }

    private fun PlayerMock.nextPlain(): String? = nextComponentMessage()?.let { PlainTextComponentSerializer.plainText().serialize(it) }

    @Test
    fun `placeholders are rendered with locale-independent size`() {
        Locale.setDefault(Locale.GERMANY)
        val player = server.addPlayer()
        catchWith(CatchAnnounceConfig(messageFormat = "<angler>:<size>:<fish_name>"))
        val message = checkNotNull(player.nextComponentMessage())
        assertEquals("Steve:12.35:", PlainTextComponentSerializer.plainText().serialize(message))
        val keys = message.iterable(ComponentIteratorType.DEPTH_FIRST).filterIsInstance<TranslatableComponent>().map { it.key() }
        assertEquals(listOf("moripa_fishing.fish.tuna.name"), keys)
    }

    @Test
    fun `the rarity weight filter keeps only rare catches`() {
        val player = server.addPlayer()
        val config = CatchAnnounceConfig(messageFormat = "caught", maxRarityWeight = 1.0)
        catchWith(config, fish = caught(common.id))
        assertNull(player.nextPlain())
        catchWith(config, fish = caught(RarityId("unknown")))
        assertNull(player.nextPlain())
        catchWith(config, fish = caught(mythic.id))
        assertEquals("caught", player.nextPlain())
    }

    @Test
    fun `ALL sends to every player`() {
        val players = listOf(server.addPlayer(), server.addPlayer())
        catchWith(CatchAnnounceConfig(messageFormat = "caught", broadcastTarget = BroadcastTarget.ALL))
        players.forEach { assertEquals("caught", it.nextPlain()) }
    }

    @Test
    fun `PERMISSION sends only to players with the permission`() {
        val plugin = MockBukkit.createMockPlugin()
        val allowed = server.addPlayer().apply { addAttachment(plugin, "catch.receive", true) }
        val denied = server.addPlayer()
        catchWith(
            CatchAnnounceConfig(
                messageFormat = "caught",
                broadcastTarget = BroadcastTarget.PERMISSION,
                broadcastPermission = "catch.receive",
            ),
        )
        assertEquals("caught", allowed.nextPlain())
        assertNull(denied.nextPlain())
    }

    @Test
    fun `SAME_WORLD sends only to anglers in the same fishing world`() {
        val here = server.addPlayer()
        val there = server.addPlayer()
        val api =
            FakeMoripaFishingAPI(
                listOf(common),
                listOf(FakeAngler("here", worldId, here), FakeAngler("there", FishingWorldId("other"), there), FakeAngler("off", null)),
            )
        catchWith(CatchAnnounceConfig(messageFormat = "caught", broadcastTarget = BroadcastTarget.SAME_WORLD), api)
        assertEquals("caught", here.nextPlain())
        assertNull(there.nextPlain())
    }
}
