package party.morino.moripafishing.api.core.fish

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import party.morino.moripafishing.api.model.fish.FishConditions
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.fish.FishItemData
import party.morino.moripafishing.api.model.fish.FishSize
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import java.util.Locale

class FishDrawPoolTest {
    private val worldId = FishingWorldId("default")
    private val common = RarityId("common")
    private val rare = RarityId("rare")

    private fun rarity(
        id: String,
        weight: Double,
    ) = RarityData(RarityId(id), mapOf(Locale.JAPAN to id), weight, "1")

    private fun fish(
        id: String,
        rarity: String,
        weight: Double = 1.0,
        conditions: FishConditions = FishConditions(),
        enabled: Boolean = true,
    ) = FishData(
        id = FishId(id),
        rarity = RarityId(rarity),
        displayName = mapOf(Locale.JAPAN to id),
        item = FishItemData("COD"),
        size = FishSize(10.0, 20.0),
        weight = weight,
        conditions = conditions,
        enabled = enabled,
    )

    /** common (90): c / rare (10): a, b の候補。ドキュメントの計算例と同じ構成。 */
    private fun examplePool() =
        FishDrawPool(
            worldId,
            WeatherType.SUNNY,
            listOf(rarity("common", 90.0), rarity("rare", 10.0)),
            listOf(fish("c", "common"), fish("a", "rare"), fish("b", "rare")),
        )

    private fun assertProbabilitiesSumToOne(pool: FishDrawPool) =
        assertEquals(1.0, pool.getFishes().sumOf { pool.getProbability(it.id) }, 1e-9)

    @Test
    fun `members are enabled fish of registered rarities that match the conditions`() {
        val pool =
            FishDrawPool(
                worldId,
                WeatherType.SUNNY,
                listOf(rarity("common", 1.0), rarity("rare", 1.0), rarity("epic", 1.0)),
                listOf(
                    fish("sunny", "common"),
                    fish("rainy", "common", conditions = FishConditions(weather = setOf(WeatherType.RAINY))),
                    fish("elsewhere", "common", conditions = FishConditions(world = setOf(FishingWorldId("other")))),
                    fish("disabled", "rare", enabled = false),
                    fish("orphan", "missing"),
                    fish("bait_only", "rare", weight = 0.0),
                ),
            )
        assertEquals(listOf(FishId("sunny"), FishId("bait_only")), pool.getFishes().map { it.id })
        assertEquals(listOf(common, rare), pool.getRarities().map { it.id })
        assertEquals(listOf(FishId("bait_only")), pool.getFishes(rare).map { it.id })
        assertTrue(pool.getFishes(RarityId("epic")).isEmpty())
        assertFalse(pool.isEmpty())
    }

    @Test
    fun `returned lists cannot be modified`() {
        val pool = examplePool()
        assertThrows<UnsupportedOperationException> { (pool.getRarities() as MutableList<RarityData>).clear() }
        assertThrows<UnsupportedOperationException> { (pool.getFishes() as MutableList<FishData>).clear() }
        assertThrows<UnsupportedOperationException> { (pool.getFishes(rare) as MutableList<FishData>).clear() }
        assertThrows<UnsupportedOperationException> {
            (pool.getFishes(rare) as MutableList<FishData>).add(fish("intruder", "rare"))
        }
        assertEquals(listOf(FishId("a"), FishId("b")), pool.getFishes(rare).map { it.id })
    }

    @Test
    fun `zeroing the last positive fish removes its rarity from the draw`() {
        val pool = examplePool()
        assertTrue(pool.multiplyFishWeight(FishId("c"), 0.0))
        assertEquals(0.5, pool.getProbability(FishId("a")), 1e-12)
        assertEquals(0.5, pool.getProbability(FishId("b")), 1e-12)
    }

    @Test
    fun `conditions are a hard filter`() {
        val pool =
            FishDrawPool(
                worldId,
                null,
                listOf(rarity("common", 1.0)),
                listOf(fish("rainy", "common", conditions = FishConditions(weather = setOf(WeatherType.RAINY)))),
            )
        assertTrue(pool.isEmpty())
        assertFalse(pool.setFishWeight(FishId("rainy"), 1.0))
        assertFalse(pool.multiplyFishWeight(FishId("rainy"), 2.0))
        assertFalse(pool.multiplyFishProbability(FishId("rainy"), 2.0))
        assertFalse(pool.setRarityWeight(common, 1.0))
        assertEquals(0.0, pool.getFishWeight(FishId("rainy")))
        assertEquals(0.0, pool.getProbability(FishId("rainy")))
    }

    @Test
    fun `weights start at the definitions`() {
        val pool = examplePool()
        assertEquals(90.0, pool.getRarityWeight(common))
        assertEquals(10.0, pool.getRarityWeight(rare))
        assertEquals(1.0, pool.getFishWeight(FishId("a")))
        assertEquals(0.05, pool.getProbability(FishId("a")), 1e-12)
        assertEquals(0.9, pool.getProbability(FishId("c")), 1e-12)
        assertProbabilitiesSumToOne(pool)
    }

    @Test
    fun `a weight-0 member has probability 0 until it is given a weight`() {
        val pool =
            FishDrawPool(
                worldId,
                WeatherType.SUNNY,
                listOf(rarity("common", 1.0), rarity("rare", 1.0)),
                listOf(fish("c", "common"), fish("bait_only", "rare", weight = 0.0)),
            )
        assertEquals(0.0, pool.getProbability(FishId("bait_only")))
        // 重みが正の魚がいないレアリティは抽選対象外なので、c が確率 1
        assertEquals(1.0, pool.getProbability(FishId("c")))
        assertTrue(pool.setFishWeight(FishId("bait_only"), 1.0))
        assertEquals(0.5, pool.getProbability(FishId("bait_only")), 1e-12)
    }

    @Test
    fun `set and multiply change the raw weights`() {
        val pool = examplePool()
        assertTrue(pool.setRarityWeight(rare, 30.0))
        assertTrue(pool.multiplyRarityWeight(rare, 2.0))
        assertEquals(60.0, pool.getRarityWeight(rare))
        assertTrue(pool.setFishWeight(FishId("a"), 3.0))
        assertTrue(pool.multiplyFishWeight(FishId("a"), 0.5))
        assertEquals(1.5, pool.getFishWeight(FishId("a")))
        assertFalse(pool.setRarityWeight(RarityId("unknown"), 1.0))
        assertFalse(pool.multiplyRarityWeight(RarityId("unknown"), 1.0))
        assertFalse(pool.setFishWeight(FishId("unknown"), 1.0))
        assertFalse(pool.multiplyFishWeight(FishId("unknown"), 1.0))
        assertFalse(pool.multiplyFishProbability(FishId("unknown"), 1.0))
    }

    @Test
    fun `multiplying the fish weight only changes its share within the rarity`() {
        val pool = examplePool()
        pool.multiplyFishWeight(FishId("a"), 2.0)
        assertEquals(0.1 * 2.0 / 3.0, pool.getProbability(FishId("a")), 1e-12)
    }

    @Test
    fun `multiplying the rarity weight changes every fish of the rarity`() {
        val pool = examplePool()
        pool.multiplyRarityWeight(rare, 2.0)
        assertEquals(20.0 / 110.0 / 2.0, pool.getProbability(FishId("a")), 1e-12)
        assertEquals(20.0 / 110.0 / 2.0, pool.getProbability(FishId("b")), 1e-12)
    }

    @Test
    fun `invalid values are rejected without changing anything`() {
        val pool = examplePool()
        for (value in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows<IllegalArgumentException> { pool.setRarityWeight(rare, value) }
            assertThrows<IllegalArgumentException> { pool.multiplyRarityWeight(rare, value) }
            assertThrows<IllegalArgumentException> { pool.setFishWeight(FishId("a"), value) }
            assertThrows<IllegalArgumentException> { pool.multiplyFishWeight(FishId("a"), value) }
            assertThrows<IllegalArgumentException> { pool.multiplyFishProbability(FishId("a"), value) }
            assertThrows<IllegalArgumentException> { pool.multiplyFishProbability(FishId("unknown"), value) }
        }
        assertEquals(10.0, pool.getRarityWeight(rare))
        assertEquals(90.0, pool.getRarityWeight(common))
        assertEquals(1.0, pool.getFishWeight(FishId("a")))
        assertEquals(1.0, pool.getFishWeight(FishId("b")))
    }

    @Test
    fun `overflow is clamped to the maximum`() {
        val pool = examplePool()
        pool.setRarityWeight(rare, Double.MAX_VALUE)
        pool.multiplyRarityWeight(rare, 2.0)
        assertEquals(Double.MAX_VALUE, pool.getRarityWeight(rare))
        pool.setRarityWeight(common, Double.MAX_VALUE)
        assertEquals(0.25, pool.getProbability(FishId("a")), 1e-12)
        assertProbabilitiesSumToOne(pool)
    }

    @Test
    fun `multiplyFishProbability scales the overall probability and keeps the other ratios`() {
        val pool = examplePool()
        assertTrue(pool.multiplyFishProbability(FishId("a"), 2.0))
        assertEquals(0.1, pool.getProbability(FishId("a")), 1e-12)
        // 他の候補は一律に (1 - 0.1) / (1 - 0.05) 倍
        val others = 0.9 / 0.95
        assertEquals(0.05 * others, pool.getProbability(FishId("b")), 1e-12)
        assertEquals(0.9 * others, pool.getProbability(FishId("c")), 1e-12)
        assertProbabilitiesSumToOne(pool)
        // ドキュメントの計算例の値
        assertEquals(10.0 * 14.0 / 9.0, pool.getRarityWeight(rare), 1e-9)
        assertEquals(19.0 / 9.0, pool.getFishWeight(FishId("a")), 1e-9)
        assertEquals(1.0, pool.getFishWeight(FishId("b")))
        assertEquals(90.0, pool.getRarityWeight(common))
    }

    @Test
    fun `multiplyFishProbability can halve a probability`() {
        val pool = examplePool()
        pool.multiplyFishProbability(FishId("c"), 0.5)
        assertEquals(0.45, pool.getProbability(FishId("c")), 1e-12)
        assertEquals(0.275, pool.getProbability(FishId("a")), 1e-12)
        assertProbabilitiesSumToOne(pool)
    }

    @Test
    fun `multiplyFishProbability on a fish alone in its rarity changes only the rarity weight`() {
        val pool = examplePool()
        pool.multiplyFishProbability(FishId("c"), 0.5)
        assertEquals(1.0, pool.getFishWeight(FishId("c")))
        assertEquals(1.0, pool.getFishWeight(FishId("a")))
        assertEquals(10.0, pool.getRarityWeight(rare))
    }

    @Test
    fun `factor 0 removes the fish and keeps the other ratios`() {
        val pool = examplePool()
        pool.multiplyFishProbability(FishId("a"), 0.0)
        assertEquals(0.0, pool.getProbability(FishId("a")))
        assertEquals(0.05 / 0.95, pool.getProbability(FishId("b")), 1e-12)
        assertEquals(0.9 / 0.95, pool.getProbability(FishId("c")), 1e-12)
        assertProbabilitiesSumToOne(pool)
    }

    @Test
    fun `factor 0 on a fish alone in its rarity keeps the rarity weight`() {
        val pool = examplePool()
        pool.multiplyFishProbability(FishId("c"), 0.0)
        assertEquals(0.0, pool.getFishWeight(FishId("c")))
        assertEquals(90.0, pool.getRarityWeight(common))
        assertEquals(0.5, pool.getProbability(FishId("a")), 1e-12)
    }

    @Test
    fun `a tiny factor never produces a negative weight`() {
        for (factor in listOf(1e-20, Double.MIN_VALUE)) {
            val pool =
                FishDrawPool(
                    worldId,
                    null,
                    listOf(rarity("common", 3.0), rarity("rare", 7.0)),
                    listOf(fish("c", "common"), fish("a", "rare")),
                )
            pool.multiplyFishProbability(FishId("a"), factor)
            pool.multiplyFishProbability(FishId("c"), factor)
            assertTrue(pool.getRarityWeight(rare) >= 0.0)
            assertTrue(pool.getRarityWeight(common) >= 0.0)
            assertTrue(pool.getProbability(FishId("a")) >= 0.0)
        }
    }

    @Test
    fun `a probability above 1 is capped and every other candidate is zeroed`() {
        val pool = examplePool()
        pool.multiplyFishProbability(FishId("a"), 100.0)
        assertEquals(1.0, pool.getProbability(FishId("a")))
        assertEquals(0.0, pool.getProbability(FishId("b")))
        assertEquals(0.0, pool.getProbability(FishId("c")))
    }

    @Test
    fun `a fish with probability 0 or 1 is left unchanged by a positive factor`() {
        val zero = examplePool()
        zero.setFishWeight(FishId("a"), 0.0)
        zero.multiplyFishProbability(FishId("a"), 10.0)
        assertEquals(0.0, zero.getFishWeight(FishId("a")))
        assertEquals(10.0, zero.getRarityWeight(rare))

        val only = FishDrawPool(worldId, null, listOf(rarity("common", 1.0)), listOf(fish("c", "common")))
        only.multiplyFishProbability(FishId("c"), 0.5)
        assertEquals(1.0, only.getProbability(FishId("c")))
        only.multiplyFishProbability(FishId("c"), 0.0)
        assertEquals(0.0, only.getProbability(FishId("c")))
    }
}
