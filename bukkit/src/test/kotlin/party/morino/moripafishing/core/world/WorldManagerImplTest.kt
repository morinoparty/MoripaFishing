package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.plugin.ServicePriority
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.plugin.PluginMock
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.config.climate.WeatherPolicy
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.config.world.WorldBinding
import party.morino.moripafishing.api.core.world.spi.WeatherSource
import party.morino.moripafishing.api.core.world.spi.WorldLifecycle
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier
import party.morino.moripafishing.api.event.world.FishingWorldDeleteEvent
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.SpawnPoint
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.world.weather.WeatherCalendar
import party.morino.moripafishing.testing.FakeConfigManager
import party.morino.moripafishing.testing.FakeDisplayNameRegistry
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class WorldManagerImplTest {
    @TempDir
    lateinit var directory: File

    private lateinit var server: ServerMock
    private lateinit var core: PluginMock
    private lateinit var owner: PluginMock
    private lateinit var store: WorldConfigStore
    private lateinit var manager: WorldManagerImpl
    private lateinit var applier: RecordingApplier
    private lateinit var lifecycle: FakeLifecycle
    private lateinit var recorder: WorldEventRecorder
    private lateinit var warnings: WarningRecorder
    private val clock = MutableClock(Instant.parse("2026-11-12T00:00:00Z"))
    private val externalKey = Key.key("test", "external")
    private lateinit var source: FakeWeatherSource
    private val names = FakeDisplayNameRegistry()

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        core = MockBukkit.createMockPlugin("core")
        owner = MockBukkit.createMockPlugin("owner")
        warnings = WarningRecorder()
        core.logger.addHandler(warnings)
        store = WorldConfigStore(directory, core.logger)
        manager = WorldManagerImpl(core, FakeConfigManager(), store, names, clock)
        applier = RecordingApplier()
        lifecycle = FakeLifecycle(server)
        source = FakeWeatherSource(externalKey, appliesToWorld = true, weather = WeatherType.SNOWY)
        val services = server.servicesManager
        services.register(WorldStateApplier::class.java, applier, owner, ServicePriority.Normal)
        val throwing = WorldStateApplier { error("boom") }
        services.register(WorldStateApplier::class.java, throwing, owner, ServicePriority.High)
        services.register(WorldLifecycle::class.java, lifecycle, owner, ServicePriority.Normal)
        services.register(WeatherSource::class.java, source, owner, ServicePriority.Normal)
        recorder = WorldEventRecorder()
        server.pluginManager.registerEvents(recorder, core)
    }

    @AfterEach
    fun tearDown() {
        manager.disable()
        MockBukkit.unmock()
    }

    private fun define(
        id: String,
        generator: String = "void",
        climate: ClimateConfig? = null,
    ) = store.save(FishingWorldConfig(FishingWorldId(id), world = WorldBinding.Managed(generator = generator), climate = climate))

    private fun key(id: String): Key = Key.key("moripafishing", id)

    private fun load(id: String) = mockWorld(key(id)).also { server.addWorld(it) }

    private fun file(id: String) = File(directory, "$id.json")

    private fun startWith(vararg ids: String) {
        ids.forEach { define(it) }
        ids.forEach { load(it) }
        manager.enable()
        manager.start()
    }

    @Test
    fun `start pushes loaded worlds and creates unloaded ones with their generator`() {
        define("loaded")
        define("missing", generator = "normal")
        load("loaded")
        manager.enable()
        // 運用開始前は何も適用しない
        assertTrue(applier.states.isEmpty())

        manager.start()

        assertNotNull(applier.last("loaded"))
        assertNotNull(applier.last("missing"))
        assertEquals(listOf(key("missing") to "normal"), lifecycle.created)
        assertTrue(recorder.events.containsAll(listOf("load:loaded", "load:missing")))
        assertEquals(2, recorder.events.count { it.startsWith("load:") })
    }

    @Test
    fun `worlds are defined before start even when not loaded`() {
        define("defined", generator = "unknown")
        manager.enable()
        assertNotNull(manager.getWorld(FishingWorldId("defined")))
        assertEquals(listOf("defined"), manager.getWorlds().map { it.getId().value })
        assertNotNull(manager.getWorldByKey(key("defined")))
        assertNull(manager.getWorldByKey(Key.key("minecraft", "defined")))
    }

    @Test
    fun `WorldLoadEvent of a defined world fires FishingWorldLoadEvent and pushes`() {
        define("later", generator = "unknown")
        manager.enable()
        manager.start()
        assertNull(applier.last("later"))

        val world = load("later")
        server.pluginManager.callEvent(WorldLoadEvent(world))

        assertNotNull(applier.last("later"))
        assertEquals(listOf("load:later"), recorder.events)
    }

    @Test
    fun `a throwing applier does not block the others`() {
        startWith("pushed")
        assertNotNull(applier.last("pushed"))
        assertTrue(warnings.messages.any { it.contains("owner") }, "missing warning: ${warnings.messages}")
    }

    @Test
    fun `updateWorldConfig persists and pushes`() {
        startWith("updated")
        val world = manager.getWorld(FishingWorldId("updated"))!!
        val changed = world.getConfig().copy(world = WorldBinding.Managed(spawn = SpawnPoint(1.0, 2.0, 3.0), border = null))

        assertTrue(manager.updateWorldConfig(changed))

        assertEquals(changed, store.loadAll().single())
        assertEquals(changed, world.getConfig())
        assertEquals(SpawnPoint(1.0, 2.0, 3.0), applier.last("updated")!!.spawn)
        assertNull(applier.last("updated")!!.border)
        assertFalse(manager.updateWorldConfig(FishingWorldConfig(FishingWorldId("undefined"))))
    }

    @Test
    fun `createWorld writes the config and fires load then create`() {
        manager.enable()
        manager.start()

        assertTrue(manager.createWorld(FishingWorldId("fresh"), "normal"))

        assertTrue(file("fresh").exists())
        assertEquals(WorldBinding.Managed(generator = "normal"), manager.getWorld(FishingWorldId("fresh"))!!.getConfig().world)
        assertEquals(listOf("load:fresh", "create:fresh"), recorder.events)
        assertNotNull(applier.last("fresh"))
        assertEquals(setOf("void", "normal"), manager.getGeneratorIds())
    }

    @Test
    fun `createWorld rolls back when the lifecycle fails`() {
        manager.enable()
        manager.start()
        lifecycle.failCreate = true

        assertFalse(manager.createWorld(FishingWorldId("failed"), "void"))

        assertFalse(file("failed").exists())
        assertNull(manager.getWorld(FishingWorldId("failed")))
        assertTrue(recorder.events.isEmpty())
    }

    @Test
    fun `createWorld rejects duplicates, invalid ids and unknown generators`() {
        startWith("taken")
        assertFalse(manager.createWorld(FishingWorldId("taken"), "void"))
        assertFalse(manager.createWorld(FishingWorldId("Bad Id"), "void"))
        assertFalse(manager.createWorld(FishingWorldId("other"), "unknown"))
        assertTrue(lifecycle.created.isEmpty())
        assertFalse(file("other").exists())
    }

    @Test
    fun `a cancelled delete event stops the delete`() {
        startWith("kept")
        server.pluginManager.registerEvents(
            object : Listener {
                @EventHandler
                fun onDelete(event: FishingWorldDeleteEvent) {
                    event.isCancelled = true
                }
            },
            core,
        )

        assertFalse(manager.deleteWorld(FishingWorldId("kept")))

        assertTrue(file("kept").exists())
        assertNotNull(server.getWorld(key("kept")))
    }

    @Test
    fun `a failed unload keeps the config`() {
        startWith("stuck")
        lifecycle.failUnload = true

        assertFalse(manager.deleteWorld(FishingWorldId("stuck")))

        assertTrue(file("stuck").exists())
        assertNotNull(manager.getWorld(FishingWorldId("stuck")))
    }

    @Test
    fun `a successful delete fires unload then deleted and removes the file`() {
        startWith("doomed")
        recorder.events.clear()

        assertTrue(manager.deleteWorld(FishingWorldId("doomed")))

        assertEquals(listOf("delete:doomed", "unload:doomed", "deleted:doomed"), recorder.events)
        assertFalse(file("doomed").exists())
        assertNull(manager.getWorld(FishingWorldId("doomed")))
        assertNull(server.getWorld(key("doomed")))
    }

    @Test
    fun `disabling the owner plugin stops applier calls`() {
        startWith("owned")
        applier.states.clear()

        server.pluginManager.disablePlugin(owner)
        manager.refresh()

        assertTrue(applier.states.isEmpty())
        assertTrue(manager.getGeneratorIds().isEmpty())
    }

    @Test
    fun `external policy uses the registered source`() {
        define("external", climate = ClimateConfig(weather = WeatherPolicy.External(externalKey)))
        load("external")
        manager.enable()
        manager.start()

        assertEquals(WeatherType.SNOWY, manager.getWorld(FishingWorldId("external"))!!.getWeather())
        assertEquals(WeatherType.SNOWY, applier.last("external")!!.weather)
    }

    @Test
    fun `a missing external source falls back to the random calendar with one warning`() {
        val missing = Key.key("test", "missing")
        define("fallback", climate = ClimateConfig(weather = WeatherPolicy.External(missing)))
        manager.enable()
        val world = manager.getWorld(FishingWorldId("fallback"))!!

        val expected = WeatherCalendar(WeatherPolicy.Random(), FishingWorldId("fallback")).at(manager.now())
        assertEquals(expected, world.getWeather())
        assertEquals(expected, world.getWeather())
        assertEquals(1, warnings.messages.count { it.contains("'test:missing'") })
    }

    @Test
    fun `weather change event fires once when the clock crosses a slot boundary`() {
        val policy = WeatherPolicy.Random(intervalHours = 1)
        define("changing", climate = ClimateConfig(weather = policy))
        val calendar = WeatherCalendar(policy, FishingWorldId("changing"))
        val zone = ZoneId.of("Asia/Tokyo")
        val start =
            generateSequence(clock.instant) { it.plus(Duration.ofHours(1)) }
                .first { calendar.at(it.atZone(zone)) != calendar.at(it.plus(Duration.ofHours(1)).atZone(zone)) }
        clock.instant = start
        manager.enable()
        manager.start()

        clock.instant = start.plus(Duration.ofHours(1))
        manager.refresh()
        manager.refresh()

        val before = calendar.at(start.atZone(zone))
        val after = calendar.at(start.plus(Duration.ofHours(1)).atZone(zone))
        assertEquals(listOf("weather:changing:$before->$after"), recorder.events.filter { it.startsWith("weather:") })
    }

    @Test
    fun `reload drops removed definitions and picks up added ones`() {
        startWith("old")
        file("old").delete()
        define("new")
        load("new")

        manager.reload()

        assertNull(manager.getWorld(FishingWorldId("old")))
        assertNotNull(manager.getWorld(FishingWorldId("new")))
        assertTrue(recorder.events.contains("load:new"))
        // 定義から外したワールドはアンロードしない
        assertNotNull(server.getWorld(key("old")))
    }

    @Test
    fun `display names follow defining, updating, creating and deleting worlds`() {
        startWith("named", "dropped")
        val named = FishingWorldId("named")
        assertEquals(setOf(named.toTranslateKey(), FishingWorldId("dropped").toTranslateKey()), names.names.keys)

        val renamed = manager.getWorld(named)!!.getConfig().copy(name = mapOf(Locale.JAPAN to "改名"))
        assertTrue(manager.updateWorldConfig(renamed))
        assertEquals(mapOf(Locale.JAPAN to "改名"), names.names[named.toTranslateKey()])

        assertTrue(manager.createWorld(FishingWorldId("fresh"), "void"))
        assertTrue(names.names.containsKey(FishingWorldId("fresh").toTranslateKey()))

        assertTrue(manager.deleteWorld(FishingWorldId("fresh")))
        file("dropped").delete()
        manager.reload()
        assertEquals(setOf(named.toTranslateKey()), names.names.keys)
    }

    @Test
    fun `ServerLoadEvent starts the world manager through its listener`() {
        define("booted")
        load("booted")
        define("created", generator = "normal")
        manager.enable()

        server.pluginManager.callEvent(ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP))

        assertNotNull(applier.last("booted"))
        assertEquals(listOf(key("created") to "normal"), lifecycle.created)
        assertNotNull(server.scheduler.pendingTasks.singleOrNull { it.owner == core })
    }

    @Test
    fun `getWorldByKey accepts a bukkit NamespacedKey`() {
        define("namespaced")
        manager.enable()
        assertNotNull(manager.getWorldByKey(NamespacedKey("moripafishing", "namespaced")))
    }

    @Test
    fun `createWorld before start is rejected and leaves nothing behind`() {
        manager.enable()

        assertFalse(manager.createWorld(FishingWorldId("early"), "void"))

        assertFalse(file("early").exists())
        assertTrue(lifecycle.created.isEmpty())
        assertTrue(recorder.events.isEmpty())
    }

    @Test
    fun `updateWorldConfig before start only saves`() {
        define("quiet")
        load("quiet")
        manager.enable()
        val changed = manager.getWorld(FishingWorldId("quiet"))!!.getConfig().copy(world = WorldBinding.Managed(border = null))

        assertTrue(manager.updateWorldConfig(changed))

        assertEquals(changed, store.loadAll().single())
        assertTrue(applier.states.isEmpty())
    }

    @Test
    fun `reload retries creating defined worlds that are still unloaded`() {
        define("typo", generator = "voidd")
        manager.enable()
        manager.start()
        assertTrue(lifecycle.created.isEmpty())

        define("typo", generator = "void")
        manager.reload()

        assertEquals(listOf(key("typo") to "void"), lifecycle.created)
        assertEquals(listOf("load:typo"), recorder.events)
    }

    @Test
    fun `a throwing weather source falls back to the calendar without stopping start`() {
        val throwingKey = Key.key("test", "throwing")
        server.servicesManager.register(
            WeatherSource::class.java,
            ThrowingWeatherSource(throwingKey),
            owner,
            ServicePriority.Normal,
        )
        define("broken", climate = ClimateConfig(weather = WeatherPolicy.External(throwingKey)))
        define("after")
        load("broken")
        load("after")
        manager.enable()

        manager.start()

        val expected = WeatherCalendar(WeatherPolicy.Random(), FishingWorldId("broken")).at(manager.now())
        assertEquals(expected, applier.last("broken")!!.weather)
        assertNotNull(applier.last("after"))
        assertEquals(expected, manager.getWorld(FishingWorldId("broken"))!!.getWeather())
        assertEquals(1, warnings.messages.count { it.contains("'test:throwing'") })
        assertNotNull(server.scheduler.pendingTasks.singleOrNull { it.owner == core })
    }

    @Test
    fun `a throwing lifecycle is treated as a failure`() {
        server.servicesManager.register(WorldLifecycle::class.java, ThrowingLifecycle(), owner, ServicePriority.Highest)
        define("unborn")
        define("after")
        load("after")
        manager.enable()

        manager.start()

        assertNotNull(applier.last("after"))
        assertTrue(manager.getGeneratorIds().isEmpty())
        assertFalse(manager.createWorld(FishingWorldId("fresh"), "void"))
        assertFalse(file("fresh").exists())
        assertFalse(manager.deleteWorld(FishingWorldId("after")))
        assertTrue(file("after").exists())
    }

    @Test
    fun `refresh decides the weather once per world and applies the same decision`() {
        val alternatingKey = Key.key("test", "alternating")
        val alternating = AlternatingWeatherSource(alternatingKey)
        server.servicesManager.register(WeatherSource::class.java, alternating, owner, ServicePriority.Normal)
        define("live", climate = ClimateConfig(weather = WeatherPolicy.External(alternatingKey)))
        load("live")
        manager.enable()
        manager.start()
        recorder.events.clear()
        val callsBefore = alternating.calls

        manager.refresh()

        assertEquals(1, alternating.calls - callsBefore)
        // イベントの新しい天候と、実際に適用させた天候が同じ決定に由来すること
        val event = recorder.events.single { it.startsWith("weather:live:") }
        assertEquals(event.substringAfter("->"), applier.last("live")!!.weather.toString())
    }
}
