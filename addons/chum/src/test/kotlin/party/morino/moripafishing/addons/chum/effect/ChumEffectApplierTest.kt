package party.morino.moripafishing.addons.chum.effect

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import party.morino.moripafishing.addons.chum.ChumFixtures
import party.morino.moripafishing.addons.chum.ChumFixtures.chum
import party.morino.moripafishing.addons.chum.ChumFixtures.location
import party.morino.moripafishing.addons.chum.FakeMoripaFishingAPI
import party.morino.moripafishing.addons.chum.MutableClock
import party.morino.moripafishing.addons.chum.config.ChumConfig
import party.morino.moripafishing.api.core.fish.FishDrawPool
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import java.time.Duration
import java.util.UUID
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class ChumEffectApplierTest {
    private val rare = RarityId("rare")
    private val warnings = mutableListOf<String>()
    private val logger =
        Logger.getAnonymousLogger().apply {
            useParentHandlers = false
            addHandler(
                object : Handler() {
                    override fun publish(record: LogRecord) {
                        warnings += record.message
                    }

                    override fun flush() = Unit

                    override fun close() = Unit
                },
            )
        }
    private val disabled = ChumFixtures.fish("off", "rare").copy(enabled = false)
    private val api = FakeMoripaFishingAPI(ChumFixtures.rarities, ChumFixtures.fishes + disabled)
    private val store = ChumEffectStore(MutableClock())
    private var config = ChumConfig()
    private val applier = ChumEffectApplier(store, { config }, api, logger)

    private fun pool() = api.getFishDrawer().createPool(ChumFixtures.world)

    private fun FishDrawPool.probability(id: String) = getProbability(FishId(id))

    @Test
    fun `rarity multipliers of different chums multiply`() {
        val pool = pool()
        applier.apply(
            pool,
            mapOf("x" to chum(rarityMultipliers = mapOf("rare" to 2.0)), "y" to chum(rarityMultipliers = mapOf("rare" to 3.0))),
        )
        assertEquals(60.0, pool.getRarityWeight(rare), 1e-9)
    }

    @Test
    fun `the combined factor is clamped to maxMultiplier`() {
        config = ChumConfig(maxMultiplier = 4.0)
        val rarityPool = pool()
        applier.apply(
            rarityPool,
            mapOf("x" to chum(rarityMultipliers = mapOf("rare" to 3.0)), "y" to chum(rarityMultipliers = mapOf("rare" to 3.0))),
        )
        assertEquals(40.0, rarityPool.getRarityWeight(rare), 1e-9)
        // 積が溢れても NaN にならず、上限で頭打ちになる
        val fishPool = pool()
        applier.apply(
            fishPool,
            mapOf("x" to chum(fishMultipliers = mapOf("a" to 1e200)), "y" to chum(fishMultipliers = mapOf("a" to 1e200))),
        )
        assertEquals(0.2, fishPool.probability("a"), 1e-9)
        assertEquals(1.0, fishPool.getFishes().sumOf { fishPool.getProbability(it.id) }, 1e-9)
    }

    @Test
    fun `fish multipliers scale the overall probability`() {
        val pool = pool()
        applier.apply(pool, mapOf("x" to chum(fishMultipliers = mapOf("a" to 2.0))))
        assertEquals(0.1, pool.probability("a"), 1e-9)
        assertEquals(1.0, pool.getFishes().sumOf { pool.getProbability(it.id) }, 1e-9)
    }

    @Test
    fun `a saturating fish multiplier leaves later fish multipliers without effect`() {
        val pool = pool()
        // 候補の並びは common の c が rare の a より先。c が確率 1 に達すると a の重みは 0 になり、a の倍率は効かない
        applier.apply(pool, mapOf("x" to chum(fishMultipliers = mapOf("a" to 2.0, "c" to 2.0))))
        assertEquals(1.0, pool.probability("c"), 1e-9)
        assertEquals(0.0, pool.probability("a"))
        assertEquals(1, warnings.size)
        assertTrue("'c'" in warnings.single())
        applier.apply(pool(), mapOf("x" to chum(fishMultipliers = mapOf("c" to 2.0))))
        assertEquals(1, warnings.size)
    }

    @Test
    fun `fish weights unlock bait-only fish before multipliers apply`() {
        val pool = pool()
        assertEquals(0.0, pool.probability("bait"))
        applier.apply(pool, mapOf("x" to chum(fishWeights = mapOf("bait" to 2.0), fishMultipliers = mapOf("bait" to 2.0))))
        // rare (0.1) の中で bait の割合は 2/4 なので確率は 0.05。確率の倍率で 0.1 になる
        assertEquals(0.1, pool.probability("bait"), 1e-9)
    }

    @Test
    fun `the largest fish weight wins when several chums set one`() {
        val pool = pool()
        applier.apply(pool, mapOf("x" to chum(fishWeights = mapOf("bait" to 2.0)), "y" to chum(fishWeights = mapOf("bait" to 5.0))))
        assertEquals(5.0, pool.getFishWeight(FishId("bait")))
    }

    @Test
    fun `unknown ids are ignored with a single warning`() {
        val unknown =
            chum(rarityMultipliers = mapOf("nope" to 2.0), fishMultipliers = mapOf("ghost" to 2.0), fishWeights = mapOf("ghost" to 1.0))
        repeat(2) { applier.apply(pool(), mapOf("x" to unknown)) }
        assertEquals(2, warnings.size)
        assertTrue(warnings.any { "'nope'" in it } && warnings.any { "'ghost'" in it })
        applier.clearWarnings()
        applier.apply(pool(), mapOf("x" to unknown))
        assertEquals(4, warnings.size)
    }

    @Test
    fun `registered fish that are not candidates are ignored silently`() {
        val pool = pool()
        applier.apply(pool, mapOf("x" to chum(fishWeights = mapOf("off" to 5.0), fishMultipliers = mapOf("off" to 2.0))))
        assertTrue(warnings.isEmpty())
        assertEquals(0.0, pool.probability("off"))
    }

    @Test
    fun `active effects are applied and effects of removed chums are skipped`() {
        val steve = AnglerId(UUID.randomUUID())
        config = ChumConfig(chums = mapOf("boost" to chum(rarityMultipliers = mapOf("rare" to 2.0))))
        store.activate("boost", ChumTarget.Angler(steve), Duration.ofMinutes(1), null)
        store.activate("removed", ChumTarget.Angler(steve), Duration.ofMinutes(1), null)
        val pool = pool()
        val applied = applier.apply(steve, location(), pool)
        assertEquals(listOf("boost"), applied.map { it.chumId })
        assertEquals(20.0, pool.getRarityWeight(rare), 1e-9)
    }
}
