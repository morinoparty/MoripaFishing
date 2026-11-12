package party.morino.moripafishing.core.fish

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
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
    ) = FishDrawerImpl(fishes, rarities, worlds, Random(42))

    @Test
    fun `a weather-restricted fish is excluded`() {
        val drawer =
            drawer(
                FakeFishManager(fish("sunny_fish", "common"), fish("rainy_fish", "common", conditions = rainy)),
                FakeRarityManager(rarity("common")),
            )
        repeat(1_000) { assertEquals(FishId("sunny_fish"), drawer.draw(worldId)?.data?.id) }
        world.currentWeather = WeatherType.RAINY
        val drawn = (1..1_000).mapNotNull { drawer.draw(worldId)?.data?.id }.toSet()
        assertEquals(setOf(FishId("sunny_fish"), FishId("rainy_fish")), drawn)
    }

    @Test
    fun `a rarity with no eligible fish is never drawn`() {
        val drawer =
            drawer(
                FakeFishManager(fish("common_fish", "common"), fish("rare_fish", "rare", conditions = rainy)),
                FakeRarityManager(rarity("common", weight = 1.0), rarity("rare", weight = 1_000.0)),
            )
        repeat(1_000) { assertEquals(RarityId("common"), drawer.draw(worldId)?.rarity?.id) }
    }

    @Test
    fun `null when nothing is eligible`() {
        val drawer =
            drawer(
                FakeFishManager(fish("disabled", "common").copy(enabled = false), fish("zero", "common", weight = 0.0)),
                FakeRarityManager(rarity("common")),
            )
        assertNull(drawer.draw(worldId))
    }

    @Test
    fun `null for an unknown world with only weather-conditioned fish`() {
        val drawer = drawer(FakeFishManager(fish("rainy_fish", "common", conditions = rainy)), FakeRarityManager(rarity("common")))
        assertNull(drawer.draw(FishingWorldId("unknown")))
    }

    @Test
    fun `a fish whose rarity is unregistered is never drawn`() {
        val rarities = FakeRarityManager(rarity("common"), rarity("rare", weight = 1_000.0))
        val drawer = drawer(FakeFishManager(fish("common_fish", "common"), fish("rare_fish", "rare")), rarities)
        rarities.unregister(RarityId("rare"))
        repeat(1_000) { assertNotEquals(FishId("rare_fish"), drawer.draw(worldId)?.data?.id) }
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
