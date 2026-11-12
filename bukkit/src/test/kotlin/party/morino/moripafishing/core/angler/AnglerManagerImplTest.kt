package party.morino.moripafishing.core.angler

import net.kyori.adventure.audience.Audience
import org.bukkit.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockbukkit.mockbukkit.MockBukkitExtension
import org.mockbukkit.mockbukkit.MockBukkitInject
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.core.world.WorldKeys
import party.morino.moripafishing.core.world.mockWorld
import party.morino.moripafishing.testing.FakeFishingWorld
import party.morino.moripafishing.testing.FakeWorldManager
import java.util.UUID

@ExtendWith(MockBukkitExtension::class)
class AnglerManagerImplTest {
    @MockBukkitInject
    lateinit var server: ServerMock

    private val worldId = FishingWorldId("default")
    private val manager = AnglerManagerImpl(FakeWorldManager(FakeFishingWorld(worldId)))

    @Test
    fun `id and name of an online player`() {
        val player = server.addPlayer("Steve")
        val angler = manager.getAngler(AnglerId(player.uniqueId))
        assertEquals(AnglerId(player.uniqueId), angler.getId())
        assertEquals("Steve", angler.getName())
    }

    @Test
    fun `an offline angler has no world, location or audience`() {
        // MockBukkit は未参加の UUID にも名前を割り当てるため、名前のフォールバックはここでは確認できない
        val angler = manager.getAngler(AnglerId(UUID.randomUUID()))
        assertNull(angler.getWorld())
        assertNull(angler.getLocation())
        assertSame(Audience.empty(), angler.getAudience())
    }

    @Test
    fun `an online angler outside a fishing world has no world or location`() {
        val player = server.addPlayer()
        val angler = manager.getAngler(AnglerId(player.uniqueId))
        assertNull(angler.getWorld())
        assertNull(angler.getLocation())
        assertSame(player, angler.getAudience())
    }

    @Test
    fun `an online angler in a fishing world has its location`() {
        val world = mockWorld(WorldKeys.of(worldId)).also(server::addWorld)
        val player = server.addPlayer()
        player.teleport(Location(world, 1.5, 64.0, -2.5, 90f, 10f))
        val angler = manager.getAngler(AnglerId(player.uniqueId))
        assertEquals(worldId, angler.getWorld()?.getId())
        assertEquals(LocationData(worldId, 1.5, 64.0, -2.5, 90.0, 10.0), angler.getLocation())
    }

    @Test
    fun `online anglers are the online players`() {
        val players = listOf(server.addPlayer(), server.addPlayer())
        assertEquals(players.map { AnglerId(it.uniqueId) }.toSet(), manager.getOnlineAnglers().map { it.getId() }.toSet())
    }
}
