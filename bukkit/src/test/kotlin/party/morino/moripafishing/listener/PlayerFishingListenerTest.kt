package party.morino.moripafishing.listener

import kotlinx.serialization.json.Json
import net.kyori.adventure.key.Key
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
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.MockBukkitExtension
import org.mockbukkit.mockbukkit.MockBukkitInject
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.config.world.WorldBinding
import party.morino.moripafishing.api.core.fish.FishDrawer
import party.morino.moripafishing.api.event.fishing.AnglerFishCaughtEvent
import party.morino.moripafishing.api.event.fishing.AnglerFishDrawEvent
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.api.model.fish.Fish
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.core.angler.AnglerManagerImpl
import party.morino.moripafishing.core.fish.FishDrawerImpl
import party.morino.moripafishing.core.fish.FishItemFactory
import party.morino.moripafishing.core.world.WorldKeys
import party.morino.moripafishing.core.world.mockWorld
import party.morino.moripafishing.testing.FakeConfigManager
import party.morino.moripafishing.testing.FakeFishManager
import party.morino.moripafishing.testing.FakeFishingWorld
import party.morino.moripafishing.testing.FakeRarityManager
import party.morino.moripafishing.testing.FakeWorldManager
import party.morino.moripafishing.testing.fish
import party.morino.moripafishing.testing.rarity
import java.util.Random

@ExtendWith(MockBukkitExtension::class)
class PlayerFishingListenerTest {
    @MockBukkitInject
    lateinit var server: ServerMock

    private val worldId = FishingWorldId("default")
    private val externalId = FishingWorldId("survival")
    private val tuna = fish("tuna", "common")
    private val salmon = fish("salmon", "common").copy(item = tuna.item.copy(material = "SALMON"))
    private val fishes = FakeFishManager(tuna, salmon)
    private val drawer = FakeFishDrawer(Fish(tuna, rarity("common"), 15.0, 0.5, 1.0))
    private lateinit var plugin: Plugin
    private lateinit var player: PlayerMock
    private lateinit var listener: PlayerFishingListener
    private lateinit var worlds: FakeWorldManager

    @BeforeEach
    fun setUp() {
        plugin = MockBukkit.createMockPlugin()
        val world = mockWorld(WorldKeys.of(worldId)).also(server::addWorld)
        player = server.addPlayer()
        player.teleport(Location(world, 0.0, 64.0, 0.0))
        val external =
            FakeFishingWorld(
                externalId,
                config = FishingWorldConfig(externalId, world = WorldBinding.External(Key.key("minecraft", "survival"))),
            )
        worlds = FakeWorldManager(FakeFishingWorld(worldId), external)
        listener = listenerWith(drawer)
    }

    private fun listenerWith(
        drawer: FishDrawer,
        catalog: FakeFishManager = fishes,
    ) = PlayerFishingListener(AnglerManagerImpl(worlds), drawer, catalog, FishItemFactory(plugin, FakeConfigManager()), plugin.logger)

    private fun fishEvent(): Pair<PlayerFishEvent, Item> {
        val drop = player.world.dropItem(player.location, ItemStack(Material.COD))
        val hook = player.world.spawn(player.location, FishHook::class.java)
        return PlayerFishEvent(player, drop, hook, PlayerFishEvent.State.CAUGHT_FISH) to drop
    }

    private fun record(onCaught: (AnglerFishCaughtEvent) -> Unit = {}): CaughtEventRecorder =
        CaughtEventRecorder(onCaught).also { server.pluginManager.registerEvents(it, plugin) }

    private fun recordDraws(onDraw: (AnglerFishDrawEvent) -> Unit = {}): DrawEventRecorder =
        DrawEventRecorder(onDraw).also { server.pluginManager.registerEvents(it, plugin) }

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
    fun `the draw event carries the context and fires before the draw`() {
        val drawnBefore = mutableListOf<Int>()
        val recorder = recordDraws { drawnBefore += drawer.drawnPools.size }
        val (event, _) = fishEvent()
        val hook = event.hook.location
        listener.onPlayerFish(event)
        val draw = recorder.events.single()
        assertEquals(listOf(0), drawnBefore)
        assertEquals(player.uniqueId, draw.getAngler().getId().value)
        assertEquals(worldId, draw.getWorld().getId())
        assertEquals(LocationData(worldId, hook.x, hook.y, hook.z, hook.yaw.toDouble(), hook.pitch.toDouble()), draw.getHookLocation())
        assertSame(draw.getPool(), drawer.drawnPools.single())
        assertEquals(worldId, draw.getPool().getWorld())
    }

    @Test
    fun `weights changed by a draw listener affect the draw`() {
        val rarities = FakeRarityManager(rarity("common", weight = 1_000.0), rarity("rare", weight = 1.0))
        val catalog = FakeFishManager(tuna, fish("bream", "rare"))
        val listener = listenerWith(FishDrawerImpl(catalog, rarities, worlds, Random(1)), catalog)
        recordDraws { it.getPool().setRarityWeight(RarityId("common"), 0.0) }
        val recorder = record()
        repeat(20) { listener.onPlayerFish(fishEvent().first) }
        assertEquals(List(20) { FishId("bream") }, recorder.results.map { it.fish })
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
        val draws = recordDraws()
        val recorder = record()
        val (event, drop) = fishEvent()
        listener.onPlayerFish(event)
        assertEquals(1, draws.events.size)
        assertFalse(event.isCancelled)
        assertEquals(null, caughtFishOf(drop))
        assertTrue(recorder.results.isEmpty())
    }

    @Test
    fun `fishing in a registered external world uses the custom fish`() {
        player.teleport(Location(mockWorld(Key.key("minecraft", "survival")).also(server::addWorld), 0.0, 64.0, 0.0))
        val (event, drop) = fishEvent()
        listener.onPlayerFish(event)
        assertEquals(externalId, caughtFishOf(drop)?.world)
    }

    @Test
    fun `fishing in an unregistered world keeps the vanilla drop`() {
        player.teleport(Location(mockWorld(Key.key("minecraft", "unregistered")).also(server::addWorld), 0.0, 64.0, 0.0))
        val recorder = record()
        val (event, drop) = fishEvent()
        listener.onPlayerFish(event)
        assertEquals(null, caughtFishOf(drop))
        assertTrue(recorder.results.isEmpty())
    }
}
