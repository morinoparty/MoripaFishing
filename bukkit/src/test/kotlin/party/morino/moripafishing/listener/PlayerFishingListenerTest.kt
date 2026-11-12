package party.morino.moripafishing.listener

import kotlinx.serialization.json.Json
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.FishHook
import org.bukkit.entity.Item
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.MockBukkitExtension
import org.mockbukkit.mockbukkit.MockBukkitInject
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import party.morino.moripafishing.api.event.fishing.AnglerFishCaughtEvent
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.api.model.fish.Fish
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.core.angler.AnglerManagerImpl
import party.morino.moripafishing.core.fish.FishItemFactory
import party.morino.moripafishing.core.world.WorldKeys
import party.morino.moripafishing.core.world.mockWorld
import party.morino.moripafishing.testing.FakeConfigManager
import party.morino.moripafishing.testing.FakeFishManager
import party.morino.moripafishing.testing.FakeFishingWorld
import party.morino.moripafishing.testing.FakeWorldManager
import party.morino.moripafishing.testing.fish
import party.morino.moripafishing.testing.rarity

@ExtendWith(MockBukkitExtension::class)
class PlayerFishingListenerTest {
    @MockBukkitInject
    lateinit var server: ServerMock

    private val worldId = FishingWorldId("default")
    private val tuna = fish("tuna", "common")
    private val salmon = fish("salmon", "common").copy(item = tuna.item.copy(material = "SALMON"))
    private val fishes = FakeFishManager(tuna, salmon)
    private val drawer = FakeFishDrawer(Fish(tuna, rarity("common"), 15.0, 0.5, 1.0))
    private lateinit var plugin: Plugin
    private lateinit var player: PlayerMock
    private lateinit var listener: PlayerFishingListener

    @BeforeEach
    fun setUp() {
        plugin = MockBukkit.createMockPlugin()
        val world = mockWorld(WorldKeys.of(worldId)).also(server::addWorld)
        player = server.addPlayer()
        player.teleport(Location(world, 0.0, 64.0, 0.0))
        val anglers = AnglerManagerImpl(FakeWorldManager(FakeFishingWorld(worldId)))
        listener = PlayerFishingListener(anglers, drawer, fishes, FishItemFactory(plugin, FakeConfigManager()), plugin.logger)
    }

    private fun fishEvent(): Pair<PlayerFishEvent, Item> {
        val drop = player.world.dropItem(player.location, ItemStack(Material.COD))
        val hook = player.world.spawn(player.location, FishHook::class.java)
        return PlayerFishEvent(player, drop, hook, PlayerFishEvent.State.CAUGHT_FISH) to drop
    }

    private fun record(onCaught: (AnglerFishCaughtEvent) -> Unit = {}): CaughtEventRecorder =
        CaughtEventRecorder(onCaught).also { server.pluginManager.registerEvents(it, plugin) }

    private fun caughtFishOf(drop: Item): CaughtFish? =
        drop.itemStack.itemMeta.persistentDataContainer
            .get(NamespacedKey(plugin, "caught_fish"), PersistentDataType.STRING)
            ?.let { Json.decodeFromString(CaughtFish.serializer(), it) }

    @Test
    fun `the drawn fish replaces the drop and fires the result event`() {
        val recorder = record()
        val (event, drop) = fishEvent()
        listener.onPlayerFish(event)
        assertEquals(Material.COD, drop.itemStack.type)
        assertEquals(FishId("tuna"), caughtFishOf(drop)?.fish)
        assertEquals(listOf(caughtFishOf(drop)), recorder.results)
    }

    @Test
    fun `cancelling the caught event cancels the fish event`() {
        val recorder = record { it.isCancelled = true }
        val (event, drop) = fishEvent()
        listener.onPlayerFish(event)
        assertTrue(event.isCancelled)
        assertEquals(null, caughtFishOf(drop))
        assertTrue(recorder.results.isEmpty())
    }

    @Test
    fun `a replaced fish is used for the item`() {
        val recorder = record { it.setCaughtFish(it.getCaughtFish().copy(fish = FishId("salmon"))) }
        val (event, drop) = fishEvent()
        listener.onPlayerFish(event)
        assertEquals(Material.SALMON, drop.itemStack.type)
        assertEquals(FishId("salmon"), recorder.results.single().fish)
    }

    @Test
    fun `an unregistered replacement keeps the vanilla drop`() {
        val recorder = record { it.setCaughtFish(it.getCaughtFish().copy(fish = FishId("ghost"))) }
        val (event, drop) = fishEvent()
        listener.onPlayerFish(event)
        assertFalse(event.isCancelled)
        assertEquals(null, caughtFishOf(drop))
        assertTrue(recorder.results.isEmpty())
    }

    @Test
    fun `a null draw keeps the vanilla drop`() {
        drawer.fish = null
        val recorder = record()
        val (event, drop) = fishEvent()
        listener.onPlayerFish(event)
        assertEquals(null, caughtFishOf(drop))
        assertTrue(recorder.results.isEmpty())
    }
}
