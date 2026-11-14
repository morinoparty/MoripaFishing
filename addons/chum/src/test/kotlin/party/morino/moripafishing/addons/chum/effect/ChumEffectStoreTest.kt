package party.morino.moripafishing.addons.chum.effect

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import party.morino.moripafishing.addons.chum.ChumFixtures.location
import party.morino.moripafishing.addons.chum.ChumFixtures.world
import party.morino.moripafishing.addons.chum.MutableClock
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import java.time.Duration
import java.util.UUID

class ChumEffectStoreTest {
    private val clock = MutableClock()
    private val store = ChumEffectStore(clock)
    private val steve = AnglerId(UUID.randomUUID())
    private val alex = AnglerId(UUID.randomUUID())
    private val minute = Duration.ofSeconds(60)

    @Test
    fun `an effect expires lazily at its expiry time`() {
        store.activate("chum", ChumTarget.Angler(steve), minute, null)
        clock.advance(Duration.ofSeconds(59))
        assertEquals(1, store.active(steve, location()).size)
        clock.advance(Duration.ofSeconds(1))
        assertTrue(store.active(steve, location()).isEmpty())
    }

    @Test
    fun `the same chum on the same target refreshes instead of stacking`() {
        val first = store.activate("chum", ChumTarget.Angler(steve), minute, 3)
        store.consumeCharge(first.effect.id)
        clock.advance(Duration.ofSeconds(50))
        val second = store.activate("chum", ChumTarget.Angler(steve), minute, 3)
        assertFalse(first.refreshed)
        assertTrue(second.refreshed)
        assertEquals(first.effect.id, second.effect.id)
        val active = store.active(steve, location()).single()
        assertEquals(3, active.remainingCharges)
        clock.advance(Duration.ofSeconds(59))
        assertEquals(1, store.active(steve, location()).size)
    }

    @Test
    fun `different chums and different targets are kept separately`() {
        store.activate("a", ChumTarget.Angler(steve), minute, null)
        store.activate("b", ChumTarget.Angler(steve), minute, null)
        store.activate("a", ChumTarget.Angler(alex), minute, null)
        assertEquals(listOf("a", "b"), store.active(steve, location()).map { it.chumId })
        assertEquals(listOf("a"), store.active(alex, location()).map { it.chumId })
    }

    @Test
    fun `an angler effect covers only its angler`() {
        store.activate("chum", ChumTarget.Angler(steve), minute, null)
        assertEquals(1, store.active(steve, location(world = FishingWorldId("other"))).size)
        assertTrue(store.active(alex, location()).isEmpty())
    }

    @Test
    fun `an area effect covers hooks within the horizontal radius in the same world`() {
        store.activate("chum", ChumTarget.Area(world, 0.0, 0.0, 5.0), minute, null)
        assertEquals(1, store.active(alex, location(3.0, 4.0)).size)
        assertEquals(1, store.active(alex, location(3.0, 4.0).copy(y = -60.0)).size)
        assertTrue(store.active(alex, location(3.0, 4.1)).isEmpty())
        assertTrue(store.active(alex, location(0.0, 0.0, FishingWorldId("other"))).isEmpty())
    }

    @Test
    fun `using an area chum inside its own area refreshes it and keeps the center`() {
        val first = store.activate("chum", ChumTarget.Area(world, 0.0, 0.0, 5.0), minute, null)
        val inside = store.activate("chum", ChumTarget.Area(world, 3.0, 0.0, 5.0), minute, null)
        assertTrue(inside.refreshed)
        assertEquals(first.effect.target, inside.effect.target)
        val outside = store.activate("chum", ChumTarget.Area(world, 20.0, 0.0, 5.0), minute, null)
        assertFalse(outside.refreshed)
    }

    @Test
    fun `overlapping areas of the same chum apply once`() {
        store.activate("chum", ChumTarget.Area(world, 0.0, 0.0, 5.0), minute, null)
        store.activate("chum", ChumTarget.Area(world, 8.0, 0.0, 5.0), minute, null)
        assertEquals(1, store.active(steve, location(4.0, 0.0)).size)
    }

    @Test
    fun `a world effect covers the whole fishing world`() {
        store.activate("chum", ChumTarget.World(world), minute, null)
        assertEquals(1, store.active(alex, location(1000.0, -1000.0)).size)
        assertTrue(store.active(alex, location(world = FishingWorldId("other"))).isEmpty())
    }

    @Test
    fun `charges are consumed one by one and the effect is removed at zero`() {
        val effect = store.activate("chum", ChumTarget.Angler(steve), minute, 2).effect
        assertEquals(1, store.consumeCharge(effect.id)?.remainingCharges)
        assertEquals(0, store.consumeCharge(effect.id)?.remainingCharges)
        assertTrue(store.active(steve, location()).isEmpty())
        assertNull(store.consumeCharge(effect.id))
    }

    @Test
    fun `effects without charges are not consumed`() {
        val effect = store.activate("chum", ChumTarget.Angler(steve), minute, null).effect
        assertNull(store.consumeCharge(effect.id))
        assertEquals(1, store.active(steve, location()).size)
    }

    @Test
    fun `expired effects cannot be consumed`() {
        val effect = store.activate("chum", ChumTarget.Angler(steve), minute, 2).effect
        clock.advance(minute)
        assertNull(store.consumeCharge(effect.id))
    }
}
