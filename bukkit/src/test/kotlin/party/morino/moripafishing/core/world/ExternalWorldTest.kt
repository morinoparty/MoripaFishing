package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.plugin.ServicePriority
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.config.climate.WeatherPolicy
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.config.world.WorldBinding
import party.morino.moripafishing.api.core.world.spi.WorldLifecycle
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.testing.FakeConfigManager
import party.morino.moripafishing.testing.FakeDisplayNameRegistry
import java.io.File
import java.time.Instant
import java.util.Locale

/**
 * 外部ワールド (`WorldBinding.External` で既存の Bukkit ワールドを登録したもの) が
 * Integration が登録されていても読み取り専用で扱われることを確認する。
 */
class ExternalWorldTest {
    @TempDir
    lateinit var directory: File

    private lateinit var server: ServerMock
    private lateinit var core: PluginMock
    private lateinit var store: WorldConfigStore
    private lateinit var manager: WorldManagerImpl
    private lateinit var applier: RecordingApplier
    private lateinit var lifecycle: FakeLifecycle
    private lateinit var recorder: WorldEventRecorder
    private lateinit var warnings: WarningRecorder
    private val survivalKey = Key.key("minecraft", "survival")
    private val survival = FishingWorldId("survival")

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        core = MockBukkit.createMockPlugin("core")
        val owner = MockBukkit.createMockPlugin("owner")
        warnings = WarningRecorder()
        core.logger.addHandler(warnings)
        store = WorldConfigStore(directory, core.logger)
        manager =
            WorldManagerImpl(
                core,
                FakeConfigManager(),
                store,
                FakeDisplayNameRegistry(),
                MutableClock(Instant.parse("2026-11-12T00:00:00Z")),
            )
        applier = RecordingApplier()
        lifecycle = FakeLifecycle(server)
        server.servicesManager.register(WorldStateApplier::class.java, applier, owner, ServicePriority.Normal)
        server.servicesManager.register(WorldLifecycle::class.java, lifecycle, owner, ServicePriority.Normal)
        recorder = WorldEventRecorder()
        server.pluginManager.registerEvents(recorder, core)
    }

    @AfterEach
    fun tearDown() {
        manager.disable()
        MockBukkit.unmock()
    }

    private fun defineExternal(
        id: String,
        worldKey: Key,
        climate: ClimateConfig? = null,
    ) = store.save(FishingWorldConfig(FishingWorldId(id), world = WorldBinding.External(worldKey), climate = climate))

    private fun file(id: String) = File(directory, "$id.json")

    @Test
    fun `a loaded external world is never applied, created or unloaded`() {
        defineExternal("survival", survivalKey)
        val bukkitWorld = mockWorld(survivalKey).also(server::addWorld)
        manager.enable()
        manager.start()
        manager.refresh()
        val world = manager.getWorld(survival)!!
        assertTrue(manager.updateWorldConfig(world.getConfig().copy(name = mapOf(Locale.JAPAN to "サバイバル"))))
        manager.reload()

        assertTrue(applier.states.isEmpty())
        assertTrue(lifecycle.created.isEmpty())
        assertEquals(listOf("load:survival"), recorder.events.filter { it.startsWith("load:") })
        assertSame(world, manager.getWorldByKey(NamespacedKey.minecraft("survival")))

        assertTrue(manager.deleteWorld(survival))
        assertSame(bukkitWorld, server.getWorld(survivalKey))
        assertFalse(file("survival").exists())
        assertEquals(listOf("delete:survival", "deleted:survival"), recorder.events.filter { it.startsWith("delete") })
    }

    @Test
    fun `an unloaded external world is not created and is announced once it loads`() {
        defineExternal("survival", survivalKey)
        manager.enable()
        manager.start()
        manager.reload()
        assertTrue(lifecycle.created.isEmpty())
        assertTrue(recorder.events.none { it.startsWith("load:") })

        val bukkitWorld = mockWorld(survivalKey).also(server::addWorld)
        server.pluginManager.callEvent(WorldLoadEvent(bukkitWorld))

        assertEquals(listOf("load:survival"), recorder.events.filter { it.startsWith("load:") })
        assertTrue(applier.states.isEmpty())
    }

    @Test
    fun `external worlds read the vanilla weather by default`() {
        defineExternal("survival", survivalKey)
        mockWorld(survivalKey).also(server::addWorld).setStorm(true)
        manager.enable()
        assertEquals(WeatherType.RAINY, manager.getWorld(survival)!!.getWeather())
    }

    @Test
    fun `an explicit climate overrides the vanilla default but is still not applied`() {
        defineExternal("survival", survivalKey, ClimateConfig(weather = WeatherPolicy.Fixed(WeatherType.SNOWY)))
        mockWorld(survivalKey).also(server::addWorld)
        manager.enable()
        manager.start()
        assertEquals(WeatherType.SNOWY, manager.getWorld(survival)!!.getWeather())
        assertTrue(applier.states.isEmpty())
    }

    @Test
    fun `a second definition bound to the same bukkit world is skipped`() {
        defineExternal("a_first", survivalKey)
        defineExternal("b_second", survivalKey)
        manager.enable()

        assertEquals(listOf("a_first"), manager.getWorlds().map { it.getId().value })
        assertEquals(1, warnings.messages.count { it.startsWith("Skipping fishing world") })
    }

    @Test
    fun `an external binding cannot use the managed namespace`() {
        assertThrows<IllegalArgumentException> { WorldBinding.External(Key.key("moripafishing", "managed")) }
    }

    @Test
    fun `updateWorldConfig cannot change the bound world`() {
        defineExternal("survival", survivalKey)
        manager.enable()
        val config = manager.getWorld(survival)!!.getConfig()
        assertThrows<IllegalArgumentException> { manager.updateWorldConfig(config.copy(world = WorldBinding.Managed())) }
        assertThrows<IllegalArgumentException> {
            manager.updateWorldConfig(config.copy(world = WorldBinding.External(Key.key("minecraft", "other"))))
        }
    }

    @Test
    fun `reload rebinds a definition whose world key changed`() {
        defineExternal("survival", survivalKey)
        manager.enable()
        val otherKey = Key.key("minecraft", "other")
        defineExternal("survival", otherKey)
        manager.reload()
        assertNull(manager.getWorldByKey(survivalKey))
        assertNotNull(manager.getWorldByKey(otherKey))
    }
}
