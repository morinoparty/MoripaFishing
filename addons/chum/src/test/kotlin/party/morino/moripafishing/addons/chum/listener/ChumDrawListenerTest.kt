package party.morino.moripafishing.addons.chum.listener

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.FishDrawPool
import party.morino.moripafishing.api.event.fishing.AnglerFishCaughtResultEvent
import party.morino.moripafishing.api.event.fishing.AnglerFishDrawEvent
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID
import java.util.logging.Logger

@ExtendWith(MockBukkitExtension::class)
class ChumDrawListenerTest {
    @MockBukkitInject
    lateinit var server: ServerMock

    private val rare = RarityId("rare")
    private val api = FakeMoripaFishingAPI(ChumFixtures.rarities, ChumFixtures.fishes)
    private val store = ChumEffectStore(MutableClock())
    private val config =
        ChumConfig(
            chums =
                mapOf(
                    "bait" to chum(charges = 2, rarityMultipliers = mapOf("rare" to 2.0)),
                    "timed" to chum(fishWeights = mapOf("bait" to 1.0)),
                ),
        )
    private val listener = ChumDrawListener(ChumEffectApplier(store, { config }, api, Logger.getAnonymousLogger()), store) { config }
    private lateinit var player: PlayerMock
    private lateinit var angler: Angler

    @BeforeEach
    fun setUp() {
        player = server.addPlayer()
        angler = FakeAngler(AnglerId(player.uniqueId), location(), player)
    }

    private fun draw(angler: Angler = this.angler): FishDrawPool {
        val pool = api.getFishDrawer().createPool(ChumFixtures.world)
        listener.onDraw(AnglerFishDrawEvent(angler, FakeAngler.world(ChumFixtures.world), location(), pool))
        return pool
    }

    private fun caught(angler: Angler = this.angler) {
        val fish =
            CaughtFish(
                uniqueId = UUID.randomUUID(),
                fish = FishId("a"),
                rarity = rare,
                size = 12.0,
                lengthRate = 0.5,
                worth = 1.0,
                angler = angler.getId(),
                world = ChumFixtures.world,
                timestamp = ZonedDateTime.now(),
            )
        listener.onCaught(AnglerFishCaughtResultEvent(angler, fish))
    }

    private fun activate(
        chumId: String,
        charges: Int? = null,
    ) = store.activate(chumId, ChumTarget.Angler(angler.getId()), Duration.ofMinutes(1), charges).effect

    private fun remainingCharges() = store.active(angler.getId(), location()).firstOrNull { it.chumId == "bait" }?.remainingCharges

    @Test
    fun `active effects change the pool of the draw`() {
        activate("bait", 2)
        activate("timed")
        val pool = draw()
        assertEquals(20.0, pool.getRarityWeight(rare), 1e-9)
        assertEquals(1.0, pool.getFishWeight(FishId("bait")))
    }

    @Test
    fun `a successful catch consumes a charge and the last one ends the effect`() {
        activate("bait", 2)
        draw()
        caught()
        assertEquals(1, remainingCharges())
        draw()
        caught()
        assertNull(remainingCharges())
        assertEquals("Chum の効果が切れました。", player.nextComponentMessage()?.let { PlainTextComponentSerializer.plainText().serialize(it) })
    }

    @Test
    fun `a draw without a catch result does not consume a charge`() {
        activate("bait", 2)
        draw()
        draw()
        caught()
        assertEquals(1, remainingCharges())
        // 抽選を伴わない釣果では減らない
        caught()
        assertEquals(1, remainingCharges())
    }

    @Test
    fun `effects that did not apply to the draw are not consumed`() {
        activate("bait", 2)
        val other = FakeAngler(AnglerId(UUID.randomUUID()), location())
        draw(other)
        caught(other)
        assertEquals(2, remainingCharges())
    }
}
