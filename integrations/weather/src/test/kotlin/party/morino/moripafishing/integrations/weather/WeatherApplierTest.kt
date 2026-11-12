package party.morino.moripafishing.integrations.weather

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.GameRule
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.bukkit.event.player.PlayerQuitEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import party.morino.moripafishing.api.core.world.spi.WorldState
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.SpawnPoint
import party.morino.moripafishing.api.model.world.WeatherType

class WeatherApplierTest {
    private fun playerIn(target: WorldMock): PlayerMock {
        val player = server.addPlayer()
        player.teleport(target.spawnLocation)
        return player
    }

    private lateinit var server: ServerMock
    private lateinit var world: WorldMock
    private lateinit var applier: WeatherApplier
    private val worldKey = Key.key("moripafishing", "fishing")

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        applier = WeatherApplier()
        world = WorldMock(WorldCreator(NamespacedKey("moripafishing", "fishing")))
        server.addWorld(world)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    private fun state(
        weather: WeatherType?,
        key: Key = worldKey,
    ) = WorldState(
        worldId = FishingWorldId(key.value()),
        worldKey = key,
        weather = weather,
        doWeatherCycle = weather == null,
        time = null,
        doDaylightCycle = true,
        border = null,
        spawn = SpawnPoint(0.0, 64.0, 0.0, 0f, 0f),
    )

    @ParameterizedTest
    @EnumSource(WeatherType::class)
    fun `every weather type maps to storm and thunder`(weather: WeatherType) {
        applier.apply(state(weather))
        val (storm, thunder) =
            when (weather) {
                WeatherType.THUNDERSTORM -> true to true
                WeatherType.RAINY, WeatherType.CLOUDY -> true to false
                WeatherType.SUNNY, WeatherType.FOGGY, WeatherType.SNOWY -> false to false
            }
        assertEquals(storm, world.hasStorm())
        assertEquals(thunder, world.isThundering)
    }

    @Test
    fun `the weather cycle gamerule is written on every call`() {
        applier.apply(state(WeatherType.SUNNY))
        assertEquals(false, world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE))
        applier.apply(state(null))
        assertEquals(true, world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE))
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        applier.apply(state(null))
        assertEquals(true, world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE))
    }

    @Test
    fun `null weather after a driven state releases the ceiling and leaves the storm alone`() {
        val player = playerIn(world)
        applier.apply(state(WeatherType.CLOUDY))
        assertTrue(world.hasStorm())
        assertTrue(applier.hasCeiling(player))

        applier.apply(state(null))

        assertFalse(applier.hasCeiling(player), "the ceiling was not restored")
        assertTrue(world.hasStorm())
        assertFalse(world.isThundering)
    }

    @Test
    fun `leaving cloudy restores the real blocks`() {
        val player = playerIn(world)
        applier.apply(state(WeatherType.CLOUDY))
        applier.apply(state(WeatherType.SUNNY))
        assertFalse(applier.hasCeiling(player))
    }

    @Test
    fun `refreshCeilings clears the ceiling of players who left the cloudy world`() {
        val player = playerIn(world)
        applier.apply(state(WeatherType.CLOUDY))
        val other = WorldMock(WorldCreator(NamespacedKey("minecraft", "other")))
        server.addWorld(other)
        player.teleport(other.spawnLocation)

        applier.refreshCeilings()

        assertFalse(applier.hasCeiling(player))
    }

    @Test
    fun `refreshCeilings resends the ceiling to players in a cloudy world`() {
        val player = playerIn(world)
        applier.apply(state(WeatherType.CLOUDY))

        applier.refreshCeilings()

        assertTrue(applier.hasCeiling(player))
    }

    @Test
    fun `quitting forgets the player's ceiling`() {
        val player = playerIn(world)
        applier.apply(state(WeatherType.CLOUDY))
        assertTrue(applier.hasCeiling(player))

        applier.onQuit(PlayerQuitEvent(player, Component.empty(), PlayerQuitEvent.QuitReason.DISCONNECTED))

        assertFalse(applier.hasCeiling(player))
    }

    @Test
    fun `releaseAll clears the storm of driven worlds`() {
        applier.apply(state(WeatherType.THUNDERSTORM))
        applier.releaseAll()
        assertFalse(world.hasStorm())
        assertFalse(world.isThundering)
    }

    @Test
    fun `cloudy does not touch the world blocks or biome`() {
        val biomeBefore = world.getBiome(0, world.seaLevel, 0)
        applier.apply(state(WeatherType.CLOUDY))
        applier.refreshCeilings()
        assertEquals(biomeBefore, world.getBiome(0, world.seaLevel, 0))
    }

    @Test
    fun `an unknown world is ignored`() {
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true)
        applier.apply(state(WeatherType.THUNDERSTORM, Key.key("moripafishing", "missing")))
        assertFalse(world.hasStorm())
        assertEquals(true, world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE))
    }
}
