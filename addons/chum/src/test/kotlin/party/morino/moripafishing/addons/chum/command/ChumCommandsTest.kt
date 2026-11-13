package party.morino.moripafishing.addons.chum.command

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.NamespacedKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.MockBukkitExtension
import org.mockbukkit.mockbukkit.MockBukkitInject
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import party.morino.moripafishing.addons.chum.ChumFixtures
import party.morino.moripafishing.addons.chum.ChumFixtures.chum
import party.morino.moripafishing.addons.chum.ChumFixtures.location
import party.morino.moripafishing.addons.chum.FakeAngler
import party.morino.moripafishing.addons.chum.FakeMoripaFishingAPI
import party.morino.moripafishing.addons.chum.MutableClock
import party.morino.moripafishing.addons.chum.config.ChumConfig
import party.morino.moripafishing.addons.chum.effect.ChumEffectApplier
import party.morino.moripafishing.addons.chum.effect.ChumEffectStore
import party.morino.moripafishing.addons.chum.effect.ChumTarget
import party.morino.moripafishing.addons.chum.item.ChumItems
import party.morino.moripafishing.api.model.angler.AnglerId
import java.time.Duration
import java.util.logging.Logger

@ExtendWith(MockBukkitExtension::class)
class ChumCommandsTest {
    @MockBukkitInject
    lateinit var server: ServerMock

    private val clock = MutableClock()
    private val store = ChumEffectStore(clock)
    private var config = ChumConfig(chums = mapOf("boost" to chum(charges = 2, rarityMultipliers = mapOf("rare" to 9.0))))
    private lateinit var items: ChumItems
    private lateinit var player: PlayerMock
    private lateinit var commands: ChumCommands

    @BeforeEach
    fun setUp() {
        items = ChumItems(NamespacedKey(MockBukkit.createMockPlugin(), "chum"))
        player = server.addPlayer()
        val angler = FakeAngler(AnglerId(player.uniqueId), location())
        val api = FakeMoripaFishingAPI(ChumFixtures.rarities, ChumFixtures.fishes, listOf(angler))
        val applier = ChumEffectApplier(store, { config }, api, Logger.getAnonymousLogger())
        commands = ChumCommands(api, applier, items, { config }, clock) { throw IllegalArgumentException("broken") }
    }

    private fun messages(): List<String> =
        generateSequence { player.nextComponentMessage() }.map { PlainTextComponentSerializer.plainText().serialize(it) }.toList()

    @Test
    fun `give adds chum items to the inventory`() {
        commands.give(player, player, "boost", 3)
        val item =
            player.inventory.contents
                .filterNotNull()
                .single()
        assertEquals(3, item.amount)
        assertEquals("boost", items.chumId(item))
        assertEquals(listOf("${player.name} に Chum を 3 個渡しました。"), messages())
    }

    @Test
    fun `give splits large amounts into full stacks`() {
        commands.give(player, player, "boost", 150)
        val stacks = player.inventory.contents.filterNotNull()
        assertEquals(listOf(64, 64, 22), stacks.map { it.amount })
        assertTrue(stacks.all { items.chumId(it) == "boost" })
    }

    @Test
    fun `give rejects unknown chums`() {
        commands.give(player, player, "nope", 1)
        assertTrue(
            player.inventory.contents
                .filterNotNull()
                .isEmpty(),
        )
        assertEquals(listOf("撒き餌 nope は設定されていません。"), messages())
    }

    @Test
    fun `odds applies active effects without consuming charges`() {
        val angler = AnglerId(player.uniqueId)
        store.activate("boost", ChumTarget.Angler(angler), Duration.ofSeconds(30), 2)
        commands.odds(player, player)
        // rare: 10 × 9 = 90、common: 90。a と b は 0.25、c は 0.5
        val lines = messages()
        assertTrue(lines.any { it.startsWith("50.000% ") }, lines.toString())
        assertEquals(2, lines.count { it.startsWith("25.000% ") }, lines.toString())
        // 餌限定の魚は確率 0 の候補として表示する
        assertTrue(lines.last().startsWith("0.000% "), lines.toString())
        assertTrue(lines.any { "残り 30 秒 / 残り 2 回" in it }, lines.toString())
        assertEquals(2, store.active(angler, location()).single().remainingCharges)
    }

    @Test
    fun `odds requires the player to be in a fishing world`() {
        val outside = server.addPlayer()
        commands.odds(player, outside)
        assertEquals(listOf("${outside.name} は釣りワールドにいません。"), messages())
    }

    @Test
    fun `a failed reload reports the error`() {
        commands.reload(player)
        assertEquals(listOf("読み直しに失敗しました: broken"), messages())
    }
}
