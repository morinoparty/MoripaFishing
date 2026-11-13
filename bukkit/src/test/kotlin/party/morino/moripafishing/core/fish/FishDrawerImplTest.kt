package party.morino.moripafishing.core.fish

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import party.morino.moripafishing.api.model.fish.FishConditions
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.testing.FakeFishManager
import party.morino.moripafishing.testing.FakeFishingWorld
import party.morino.moripafishing.testing.FakeRarityManager
import party.morino.moripafishing.testing.FakeWorldManager
import party.morino.moripafishing.testing.fish
import party.morino.moripafishing.testing.rarity
import java.util.Random

class FishDrawerImplTest {
    private val worldId = FishingWorldId("default")
    private val world = FakeFishingWorld(worldId, WeatherType.SUNNY)
    private val worlds = FakeWorldManager(world)
    private val rainy = FishConditions(weather = setOf(WeatherType.RAINY))

    private fun drawer(
        fishes: FakeFishManager,
        rarities: FakeRarityManager,
        seed: Long = 42,
    ) = FishDrawerImpl(fishes, rarities, worlds, Random(seed))

    private fun FishDrawerImpl.drawIn(world: FishingWorldId = worldId) = draw(createPool(world))

    @Test
    fun `an unmodified pool draws the same sequence as the previous algorithm`() {
        val fishes =
            FakeFishManager(
                fish("a", "common", weight = 3.0),
                fish("b", "common", weight = 0.0),
                fish("c", "rare", weight = 2.0),
                fish("d", "rare", weight = 5.0, conditions = rainy),
                fish("e", "epic").copy(enabled = false),
                fish("f", "legend", weight = 1.5),
                fish("g", "common", weight = 0.5),
            )
        val rarities =
            FakeRarityManager(
                rarity("common", weight = 60.0),
                rarity("rare", weight = 30.0),
                rarity("epic", weight = 9.0),
                rarity("legend", weight = 1.0),
            )
        val drawer = drawer(fishes, rarities, seed = 7)
        val reference = Random(7)
        repeat(10_000) {
            // 変更前の draw(world) と同じ手順
            val eligible =
                fishes
                    .getAll()
                    .filter { it.enabled && it.weight > 0.0 && it.conditions.matches(worldId, WeatherType.SUNNY) }
                    .groupBy { it.rarity }
            val rarity = checkNotNull(rarities.getAll().filter { it.id in eligible }.pickWeighted(reference) { it.weight })
            val expected = checkNotNull(eligible.getValue(rarity.id).pickWeighted(reference) { it.weight })
            val drawn = checkNotNull(drawer.drawIn())
            // サイズの抽選が消費する乱数をそろえる
            assertEquals(SizeDistribution.sample(expected.size, reference), drawn.size)
            assertEquals(expected.id, drawn.data.id)
        }
    }

    @Test
    fun `a weather-restricted fish is excluded`() {
        val drawer =
            drawer(
                FakeFishManager(fish("sunny_fish", "common"), fish("rainy_fish", "common", conditions = rainy)),
                FakeRarityManager(rarity("common")),
            )
        repeat(1_000) { assertEquals(FishId("sunny_fish"), drawer.drawIn()?.data?.id) }
        world.currentWeather = WeatherType.RAINY
        val drawn = (1..1_000).mapNotNull { drawer.drawIn()?.data?.id }.toSet()
        assertEquals(setOf(FishId("sunny_fish"), FishId("rainy_fish")), drawn)
    }

    @Test
    fun `a rarity with no eligible fish is never drawn`() {
        val drawer =
            drawer(
                FakeFishManager(fish("common_fish", "common"), fish("rare_fish", "rare", conditions = rainy)),
                FakeRarityManager(rarity("common", weight = 1.0), rarity("rare", weight = 1_000.0)),
            )
        repeat(1_000) { assertEquals(RarityId("common"), drawer.drawIn()?.rarity?.id) }
    }

    @Test
    fun `a rarity whose fish all have weight 0 is never drawn`() {
        val drawer =
            drawer(
                FakeFishManager(fish("common_fish", "common"), fish("bait_only", "rare", weight = 0.0)),
                FakeRarityManager(rarity("common", weight = 1.0), rarity("rare", weight = 1_000.0)),
            )
        repeat(1_000) { assertEquals(RarityId("common"), drawer.drawIn()?.rarity?.id) }
    }

    @Test
    fun `a revived weight-0 fish can be drawn`() {
        val drawer =
            drawer(
                FakeFishManager(fish("common_fish", "common"), fish("bait_only", "rare", weight = 0.0)),
                FakeRarityManager(rarity("common", weight = 1.0), rarity("rare", weight = 1.0)),
            )
        val drawn =
            (1..1_000)
                .mapNotNull {
                    val pool = drawer.createPool(worldId)
                    pool.setFishWeight(FishId("bait_only"), 1.0)
                    drawer.draw(pool)?.data?.id
                }.toSet()
        assertTrue(FishId("bait_only") in drawn)
    }

    @Test
    fun `weights changed on the pool are used`() {
        val drawer =
            drawer(
                FakeFishManager(fish("common_fish", "common"), fish("rare_fish", "rare")),
                FakeRarityManager(rarity("common", weight = 1_000.0), rarity("rare", weight = 1.0)),
            )
        repeat(1_000) {
            val pool = drawer.createPool(worldId)
            pool.setRarityWeight(RarityId("common"), 0.0)
            assertEquals(FishId("rare_fish"), drawer.draw(pool)?.data?.id)
        }
    }

    @Test
    fun `null when nothing is eligible`() {
        val drawer =
            drawer(
                FakeFishManager(fish("disabled", "common").copy(enabled = false), fish("zero", "common", weight = 0.0)),
                FakeRarityManager(rarity("common")),
            )
        assertNull(drawer.drawIn())
    }

    @Test
    fun `null for an empty pool`() {
        val drawer = drawer(FakeFishManager(), FakeRarityManager(rarity("common")))
        val pool = drawer.createPool(worldId)
        assertTrue(pool.isEmpty())
        assertNull(drawer.draw(pool))
    }

    @Test
    fun `null when every weight is set to 0`() {
        val drawer = drawer(FakeFishManager(fish("tuna", "common")), FakeRarityManager(rarity("common")))
        val pool = drawer.createPool(worldId)
        pool.setFishWeight(FishId("tuna"), 0.0)
        assertNull(drawer.draw(pool))
    }

    @Test
    fun `the pool carries the world and its weather`() {
        val drawer = drawer(FakeFishManager(), FakeRarityManager())
        val pool = drawer.createPool(worldId)
        assertEquals(worldId, pool.getWorld())
        assertEquals(WeatherType.SUNNY, pool.getWeather())
        assertNull(drawer.createPool(FishingWorldId("unknown")).getWeather())
    }

    @Test
    fun `null for an unknown world with only weather-conditioned fish`() {
        val drawer = drawer(FakeFishManager(fish("rainy_fish", "common", conditions = rainy)), FakeRarityManager(rarity("common")))
        assertNull(drawer.drawIn(FishingWorldId("unknown")))
    }

    @Test
    fun `a fish whose rarity is unregistered is never drawn`() {
        val rarities = FakeRarityManager(rarity("common"), rarity("rare", weight = 1_000.0))
        val drawer = drawer(FakeFishManager(fish("common_fish", "common"), fish("rare_fish", "rare")), rarities)
        rarities.unregister(RarityId("rare"))
        repeat(1_000) { assertNotEquals(FishId("rare_fish"), drawer.drawIn()?.data?.id) }
    }

    @Test
    fun `roll uses the fish formula, else the rarity formula`() {
        val drawer = drawer(FakeFishManager(), FakeRarityManager(rarity("common", worth = "100")))
        assertEquals(7.0, drawer.roll(fish("own", "common", worth = "7")).worth)
        assertEquals(100.0, drawer.roll(fish("inherited", "common")).worth)
    }

    @Test
    fun `roll with an unregistered rarity throws`() {
        val drawer = drawer(FakeFishManager(), FakeRarityManager())
        assertThrows<IllegalArgumentException> { drawer.roll(fish("orphan", "missing")) }
    }
}
