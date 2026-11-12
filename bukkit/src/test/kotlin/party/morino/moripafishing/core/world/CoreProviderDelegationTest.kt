package party.morino.moripafishing.core.world

import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockbukkit.mockbukkit.world.WorldMock
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.config.climate.ConstantConfig
import party.morino.moripafishing.api.config.world.WorldDetailConfig
import party.morino.moripafishing.api.core.world.weather.WeatherSource
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.integrations.weather.api.WeatherControlProvider
import party.morino.moripafishing.integrations.worldlifecycle.api.GeneratorData
import party.morino.moripafishing.integrations.worldlifecycle.api.WorldLifecycleProvider
import party.morino.moripafishing.utils.Utils

/**
 * Integration 導入時に、コアが決定した値 (ゲームルール・時刻・スポーン・アンロード) を
 * 正しく Integration へ渡していることを確認するテスト。
 *
 * 記録用のフェイクプロバイダーを `MoripaFishing` の解決済みプロバイダーとして差し込む。
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@ExtendWith(MoripaFishingTest::class)
class CoreProviderDelegationTest : KoinTest {
    private val plugin: MoripaFishing by inject()
    private val pluginDirectory: PluginDirectory by inject()

    private val worldId = FishingWorldId("core_delegation_test")
    private val worldKey = worldId.toWorldKey().asString()
    private lateinit var world: WorldMock
    private lateinit var lifecycle: RecordingWorldLifecycleProvider
    private lateinit var weather: RecordingWeatherControlProvider

    @BeforeEach
    fun setUp() {
        world = WorldMock(WorldCreator(NamespacedKey.fromString(worldKey)!!))
        MoripaFishingTest.server.addWorld(world)
        lifecycle = RecordingWorldLifecycleProvider()
        weather = RecordingWeatherControlProvider()
        setProvider("worldLifecycleProvider", lifecycle)
        setProvider("weatherControlProvider", weather)
    }

    @AfterEach
    fun tearDown() {
        setProvider("worldLifecycleProvider", null)
        setProvider("weatherControlProvider", null)
        if (MoripaFishingTest.server.getWorld(worldId.toWorldKey()) != null) {
            MoripaFishingTest.server.unloadWorld(world, false)
        }
        configFile().delete()
    }

    private fun setProvider(
        fieldName: String,
        value: Any?,
    ) {
        val field = MoripaFishing::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(plugin, value)
    }

    private fun configFile() = pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")

    private fun writeConfig(climateConfig: ClimateConfig) {
        val config =
            WorldDetailConfig(
                id = worldId,
                name = mapOf(java.util.Locale.JAPAN to worldId.value),
                spawnLocation = LocationData(worldId, 0.0, 64.0, 0.0, 90.0, 0.0),
                climateConfig = climateConfig,
            )
        configFile().writeText(Utils.json.encodeToString(WorldDetailConfig.serializer(), config))
    }

    @Test
    fun `day cycle enabled disables vanilla daylight cycle and forwards the computed time`() {
        // constant.dayCycle = 12 -> (12 * 1000 + 18000) % 24000 = 6000
        writeConfig(ClimateConfig(enableDayCycle = true, constant = ConstantConfig(dayCycle = 12)))
        FishingWorldImpl(worldId).updateState()

        assertEquals(listOf(worldKey to false), lifecycle.daylightCycleCalls)
        assertEquals(listOf(worldKey to 6000L), lifecycle.timeCalls)
    }

    @Test
    fun `day cycle disabled enables vanilla daylight cycle and does not forward time`() {
        writeConfig(ClimateConfig(enableDayCycle = false, constant = ConstantConfig(dayCycle = 12)))
        FishingWorldImpl(worldId).updateState()

        assertEquals(listOf(worldKey to true), lifecycle.daylightCycleCalls)
        assertTrue(lifecycle.timeCalls.isEmpty(), "time must not be forwarded: ${lifecycle.timeCalls}")
    }

    @Test
    fun `weather cycle follows usesVanillaWeatherCycle of the source`() {
        writeConfig(ClimateConfig(weatherSource = WeatherSource.INTERNAL, applyWeatherEffects = true))
        FishingWorldImpl(worldId).updateState()
        assertEquals(listOf(worldKey to false), weather.weatherCycleCalls)

        weather.weatherCycleCalls.clear()
        writeConfig(ClimateConfig(weatherSource = WeatherSource.VANILLA, applyWeatherEffects = true))
        val vanillaWorld = FishingWorldImpl(worldId)
        try {
            vanillaWorld.updateState()
            assertEquals(listOf(worldKey to true), weather.weatherCycleCalls)
        } finally {
            vanillaWorld.disposeWeatherProvider()
        }
    }

    @Test
    fun `applyWeatherEffects disabled releases the weather cycle exactly once`() {
        writeConfig(ClimateConfig(applyWeatherEffects = false))
        val fishingWorld = FishingWorldImpl(worldId)
        fishingWorld.updateState()
        fishingWorld.updateState()

        assertEquals(listOf(worldKey to true), weather.weatherCycleCalls)
        assertTrue(weather.appliedWeathers.isEmpty(), "weather must not be applied: ${weather.appliedWeathers}")
    }

    @Test
    fun `setWorldSpawnPosition forwards the location to the integration`() {
        writeConfig(ClimateConfig())
        val fishingWorld = FishingWorldImpl(worldId)
        fishingWorld.setWorldSpawnPosition(LocationData(worldId, 10.0, 70.0, -20.0, 45.0, 15.0))

        assertEquals(
            listOf(SpawnCall(worldKey, 10.0, 70.0, -20.0, 45.0f, 15.0f)),
            lifecycle.spawnCalls,
        )
        assertEquals(10.0, fishingWorld.getWorldSpawnPosition().x)
    }

    @Test
    fun `deleteWorld keeps the config when the integration fails to unload`() {
        writeConfig(ClimateConfig())
        lifecycle.unloadResult = false

        assertFalse(WorldManagerImpl().deleteWorld(worldId))
        assertEquals(listOf(worldKey to false), lifecycle.unloadCalls)
        assertTrue(configFile().exists())
        assertNotNull(MoripaFishingTest.server.getWorld(worldId.toWorldKey()))
    }

    @Test
    fun `deleteWorld removes the config when the integration unloads the world`() {
        writeConfig(ClimateConfig())
        lifecycle.unloadResult = true

        assertTrue(WorldManagerImpl().deleteWorld(worldId))
        assertEquals(listOf(worldKey to false), lifecycle.unloadCalls)
        assertFalse(configFile().exists())
    }

    private data class SpawnCall(
        val worldKey: String,
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        val pitch: Float,
    )

    private class RecordingWorldLifecycleProvider : WorldLifecycleProvider {
        val daylightCycleCalls = mutableListOf<Pair<String, Boolean>>()
        val timeCalls = mutableListOf<Pair<String, Long>>()
        val spawnCalls = mutableListOf<SpawnCall>()
        val unloadCalls = mutableListOf<Pair<String, Boolean>>()
        var unloadResult = false

        override fun applyBorder(
            worldKey: String,
            centerX: Double,
            centerZ: Double,
            size: Double,
        ) = Unit

        override fun createBukkitWorld(
            worldKey: String,
            generatorData: GeneratorData,
        ): Boolean = false

        override fun getGenerator(id: String): GeneratorData? = null

        override fun listGenerators(): List<GeneratorData> = emptyList()

        override fun addGenerator(generator: GeneratorData) = Unit

        override fun setDaylightCycle(
            worldKey: String,
            enabled: Boolean,
        ) {
            daylightCycleCalls += worldKey to enabled
        }

        override fun setTime(
            worldKey: String,
            time: Long,
        ) {
            timeCalls += worldKey to time
        }

        override fun setSpawnLocation(
            worldKey: String,
            x: Double,
            y: Double,
            z: Double,
            yaw: Float,
            pitch: Float,
        ) {
            spawnCalls += SpawnCall(worldKey, x, y, z, yaw, pitch)
        }

        override fun unloadBukkitWorld(
            worldKey: String,
            save: Boolean,
        ): Boolean {
            unloadCalls += worldKey to save
            return unloadResult
        }
    }

    private class RecordingWeatherControlProvider : WeatherControlProvider {
        val weatherCycleCalls = mutableListOf<Pair<String, Boolean>>()
        val appliedWeathers = mutableListOf<Pair<String, String>>()

        override fun applyWeather(
            worldKey: String,
            weatherType: String,
        ) {
            appliedWeathers += worldKey to weatherType
        }

        override fun resetWeather(worldKey: String) = Unit

        override fun setWeatherCycle(
            worldKey: String,
            enabled: Boolean,
        ) {
            weatherCycleCalls += worldKey to enabled
        }
    }
}
