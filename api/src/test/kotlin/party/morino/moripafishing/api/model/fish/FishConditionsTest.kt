package party.morino.moripafishing.api.model.fish

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

class FishConditionsTest {
    private val world = FishingWorldId("default")
    private val other = FishingWorldId("other")

    @Test
    fun `empty conditions always match`() {
        assertTrue(FishConditions().matches(world, WeatherType.SUNNY))
        assertTrue(FishConditions().matches(world, null))
    }

    @Test
    fun `world condition`() {
        val conditions = FishConditions(world = setOf(world))
        assertTrue(conditions.matches(world, WeatherType.SUNNY))
        assertFalse(conditions.matches(other, WeatherType.SUNNY))
    }

    @Test
    fun `weather condition`() {
        val conditions = FishConditions(weather = setOf(WeatherType.RAINY))
        assertTrue(conditions.matches(world, WeatherType.RAINY))
        assertFalse(conditions.matches(world, WeatherType.SUNNY))
    }

    @Test
    fun `unknown weather does not match a weather condition`() {
        assertFalse(FishConditions(weather = setOf(WeatherType.RAINY)).matches(world, null))
    }
}
