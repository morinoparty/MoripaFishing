package party.morino.moripafishing.addons.chum.listener

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
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
import party.morino.moripafishing.addons.chum.ChumPermissions
import party.morino.moripafishing.addons.chum.FakeAngler
import party.morino.moripafishing.addons.chum.FakeMoripaFishingAPI
import party.morino.moripafishing.addons.chum.MutableClock
import party.morino.moripafishing.addons.chum.config.ChumConfig
import party.morino.moripafishing.addons.chum.config.ChumScope
import party.morino.moripafishing.addons.chum.effect.ChumEffectStore
import party.morino.moripafishing.addons.chum.effect.ChumTarget
import party.morino.moripafishing.addons.chum.item.ChumItems
import party.morino.moripafishing.api.model.angler.AnglerId
import java.time.Duration

@ExtendWith(MockBukkitExtension::class)
class ChumUseListenerTest {
    @MockBukkitInject
    lateinit var server: ServerMock

    private val clock = MutableClock()
    private val store = ChumEffectStore(clock)
    private var config =
        ChumConfig(
            chums =
                mapOf(
                    "angler" to chum(charges = 3),
                    "area" to chum(scope = ChumScope.AREA, radius = 8.0),
                    "world" to chum(scope = ChumScope.WORLD),
                ),
        )
    private lateinit var items: ChumItems
    private lateinit var player: PlayerMock
    private lateinit var anglerId: AnglerId

    @BeforeEach
    fun setUp() {
        // paper-plugin.yml と同じく、使用権限は全員に与える
        server.pluginManager.addPermission(Permission(ChumPermissions.USE, PermissionDefault.TRUE))
        items = ChumItems(NamespacedKey(MockBukkit.createMockPlugin(), "chum"))
        player = server.addPlayer()
        anglerId = AnglerId(player.uniqueId)
    }

    private fun listener(inFishingWorld: Boolean = true): ChumUseListener {
        val location = if (inFishingWorld) location(10.0, 20.0) else null
        val api = FakeMoripaFishingAPI(anglers = listOf(FakeAngler(anglerId, location, player)))
        return ChumUseListener(api, store, items, { config }, clock)
    }

    private val listener by lazy { listener() }

    private fun hold(
        chumId: String,
        amount: Int = 2,
    ) {
        player.inventory.setItemInMainHand(items.create(chumId, chum(), amount))
    }

    private fun rightClick(
        action: Action = Action.RIGHT_CLICK_AIR,
        hand: EquipmentSlot = EquipmentSlot.HAND,
        listener: ChumUseListener = this.listener,
    ): PlayerInteractEvent {
        val block = if (action == Action.RIGHT_CLICK_BLOCK) server.addSimpleWorld("world").getBlockAt(0, 64, 0) else null
        val event = PlayerInteractEvent(player, action, player.inventory.itemInMainHand, block, BlockFace.UP, hand)
        listener.onInteract(event)
        return event
    }

    private fun nextMessage(): String? = player.nextComponentMessage()?.let { PlainTextComponentSerializer.plainText().serialize(it) }

    private fun effects() = store.active(anglerId, location(10.0, 20.0))

    @Test
    fun `right-clicking the air consumes one item and activates the effect`() {
        hold("angler")
        val event = rightClick()
        assertTrue(event.isCancelled)
        assertEquals(1, player.inventory.itemInMainHand.amount)
        val effect = effects().single()
        assertEquals(ChumTarget.Angler(anglerId), effect.target)
        assertEquals(3, effect.remainingCharges)
        assertEquals("Chum を使いました。あなたの釣りに 60 秒間効果があります（釣果 3 回まで）。", nextMessage())
    }

    @Test
    fun `right-clicking a block also uses the chum and cancels the block interaction`() {
        hold("angler")
        val event = rightClick(Action.RIGHT_CLICK_BLOCK)
        assertEquals(Event.Result.DENY, event.useInteractedBlock())
        assertEquals(1, effects().size)
    }

    @Test
    fun `using the same chum again refreshes the effect`() {
        hold("angler")
        rightClick()
        nextMessage()
        clock.advance(Duration.ofSeconds(1))
        rightClick()
        assertEquals(1, effects().size)
        assertTrue(nextMessage().orEmpty().startsWith("Chum の効果を更新しました。"))
        assertEquals(Material.AIR, player.inventory.itemInMainHand.type)
    }

    @Test
    fun `holding right-click does not use the same chum twice`() {
        hold("angler", 3)
        rightClick()
        val repeated = rightClick()
        assertTrue(repeated.isCancelled)
        assertEquals(2, player.inventory.itemInMainHand.amount)
        clock.advance(Duration.ofMillis(999))
        rightClick()
        assertEquals(2, player.inventory.itemInMainHand.amount)
        clock.advance(Duration.ofMillis(1))
        rightClick()
        assertEquals(1, player.inventory.itemInMainHand.amount)
    }

    @Test
    fun `area and world chums target the fishing world position`() {
        hold("area")
        rightClick()
        hold("world")
        rightClick()
        assertEquals(
            listOf(ChumTarget.Area(ChumFixtures.world, 10.0, 20.0, 8.0), ChumTarget.World(ChumFixtures.world)),
            effects().map { it.target },
        )
    }

    @Test
    fun `area chums are not used outside a fishing world`() {
        hold("area")
        rightClick(listener = listener(inFishingWorld = false))
        assertEquals(2, player.inventory.itemInMainHand.amount)
        assertTrue(effects().isEmpty())
        assertEquals("この撒き餌は釣りワールドでのみ使えます。", nextMessage())
    }

    @Test
    fun `angler chums can be used outside a fishing world`() {
        hold("angler")
        rightClick(listener = listener(inFishingWorld = false))
        assertEquals(1, effects().size)
    }

    @Test
    fun `chums removed from the config are not consumed`() {
        hold("removed")
        val event = rightClick()
        assertTrue(event.isCancelled)
        assertEquals(2, player.inventory.itemInMainHand.amount)
        assertTrue(effects().isEmpty())
    }

    @Test
    fun `the off hand, left clicks and denied item use are ignored`() {
        hold("angler")
        rightClick(hand = EquipmentSlot.OFF_HAND)
        rightClick(Action.LEFT_CLICK_AIR)
        val denied = PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, player.inventory.itemInMainHand, null, BlockFace.UP)
        denied.setUseItemInHand(Event.Result.DENY)
        listener().onInteract(denied)
        assertEquals(2, player.inventory.itemInMainHand.amount)
        assertTrue(effects().isEmpty())
        assertNull(nextMessage())
    }

    @Test
    fun `other items are left alone`() {
        player.inventory.setItemInMainHand(ItemStack(Material.COD))
        val event = rightClick(Action.RIGHT_CLICK_BLOCK)
        assertFalse(event.useInteractedBlock() == Event.Result.DENY)
        assertTrue(effects().isEmpty())
    }

    @Test
    fun `players without the use permission cannot use chums`() {
        player.addAttachment(MockBukkit.createMockPlugin(), ChumPermissions.USE, false)
        hold("angler")
        rightClick()
        assertEquals(2, player.inventory.itemInMainHand.amount)
        assertTrue(effects().isEmpty())
    }
}
