package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import party.morino.moripafishing.api.config.ConfigData
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.world.spi.WeatherSource
import party.morino.moripafishing.api.core.world.spi.WorldLifecycle
import party.morino.moripafishing.api.core.world.spi.WorldState
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.event.world.FishingWorldCreateEvent
import party.morino.moripafishing.event.world.FishingWorldDeleteEvent
import party.morino.moripafishing.event.world.FishingWorldDeletedEvent
import party.morino.moripafishing.event.world.FishingWorldLoadEvent
import party.morino.moripafishing.event.world.FishingWorldUnloadEvent
import party.morino.moripafishing.event.world.FishingWorldWeatherChangeEvent
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.logging.Handler
import java.util.logging.LogRecord

/** 固定の [ConfigData] を返す [ConfigManager]。 */
internal class FakeConfigManager(
    var data: ConfigData = ConfigData(),
) : ConfigManager {
    override fun reload() = Unit

    override fun getConfig(): ConfigData = data

    override fun applyConfig(configData: ConfigData) {
        data = configData
    }
}

/** テストから進められる [Clock]。 */
internal class MutableClock(
    var instant: Instant,
    private val zone: ZoneId = ZoneOffset.UTC,
) : Clock() {
    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableClockView(this, zone)

    override fun instant(): Instant = instant

    private class MutableClockView(
        private val source: MutableClock,
        private val zone: ZoneId,
    ) : Clock() {
        override fun getZone(): ZoneId = zone

        override fun withZone(zone: ZoneId): Clock = MutableClockView(source, zone)

        override fun instant(): Instant = source.instant
    }
}

/** 受け取った [WorldState] を記録する [WorldStateApplier]。 */
internal class RecordingApplier : WorldStateApplier {
    val states = mutableListOf<WorldState>()

    override fun apply(state: WorldState) {
        states += state
    }

    fun last(id: String): WorldState? = states.lastOrNull { it.worldId == FishingWorldId(id) }
}

/** MockBukkit 上でワールドを作成・アンロードする [WorldLifecycle]。 */
internal class FakeLifecycle(
    private val server: ServerMock,
    private val generators: Set<String> = setOf("void", "normal"),
) : WorldLifecycle {
    val created = mutableListOf<Pair<Key, String>>()
    var failCreate = false
    var failUnload = false

    override fun getGeneratorIds(): Set<String> = generators

    override fun createWorld(
        worldKey: Key,
        generatorId: String,
    ): Boolean {
        if (failCreate || generatorId !in generators || server.getWorld(worldKey) != null) return false
        created += worldKey to generatorId
        val world = mockWorld(worldKey)
        server.addWorld(world)
        server.pluginManager.callEvent(WorldLoadEvent(world))
        return true
    }

    override fun unloadWorld(worldKey: Key): Boolean {
        val world = server.getWorld(worldKey)
        return !failUnload && world != null && server.unloadWorld(world, false)
    }
}

/** 固定の天候を返す [WeatherSource]。 */
internal class FakeWeatherSource(
    override val key: Key,
    override val appliesToWorld: Boolean,
    var weather: WeatherType,
) : WeatherSource {
    override fun getWeather(
        worldId: FishingWorldId,
        worldKey: Key,
    ): WeatherType = weather
}

/** 呼ばれるたびに交互の天候を返し、呼び出し回数を数える [WeatherSource]。 */
internal class AlternatingWeatherSource(
    override val key: Key,
) : WeatherSource {
    override val appliesToWorld = true
    var calls = 0

    override fun getWeather(
        worldId: FishingWorldId,
        worldKey: Key,
    ): WeatherType = if (calls++ % 2 == 0) WeatherType.RAINY else WeatherType.SUNNY
}

/** 常に例外を投げる [WeatherSource]。 */
internal class ThrowingWeatherSource(
    override val key: Key,
) : WeatherSource {
    override val appliesToWorld = true

    override fun getWeather(
        worldId: FishingWorldId,
        worldKey: Key,
    ): WeatherType = error("weather backend is down")
}

/** すべてのメソッドで例外を投げる [WorldLifecycle]。 */
internal class ThrowingLifecycle : WorldLifecycle {
    override fun getGeneratorIds(): Set<String> = error("generator ids are unavailable")

    override fun createWorld(
        worldKey: Key,
        generatorId: String,
    ): Boolean = error("cannot create")

    override fun unloadWorld(worldKey: Key): Boolean = error("cannot unload")
}

/** 釣りワールドのイベントを発火順に記録する。 */
internal class WorldEventRecorder : Listener {
    val events = mutableListOf<String>()

    @EventHandler
    fun onLoad(event: FishingWorldLoadEvent) {
        events += "load:${event.getWorldId().value}"
    }

    @EventHandler
    fun onUnload(event: FishingWorldUnloadEvent) {
        events += "unload:${event.getWorldId().value}"
    }

    @EventHandler
    fun onCreate(event: FishingWorldCreateEvent) {
        events += "create:${event.getWorldId().value}"
    }

    @EventHandler
    fun onDelete(event: FishingWorldDeleteEvent) {
        events += "delete:${event.getWorldId().value}"
    }

    @EventHandler
    fun onDeleted(event: FishingWorldDeletedEvent) {
        events += "deleted:${event.getWorldId().value}"
    }

    @EventHandler
    fun onWeatherChange(event: FishingWorldWeatherChangeEvent) {
        events += "weather:${event.getWorldId().value}:${event.getOldWeather()}->${event.getNewWeather()}"
    }
}

/** 警告ログのメッセージを記録する。 */
internal class WarningRecorder : Handler() {
    val messages = mutableListOf<String>()

    override fun publish(record: LogRecord) {
        messages += record.message
    }

    override fun flush() = Unit

    override fun close() = Unit
}

/** 指定キーの [WorldMock] を作る。 */
internal fun mockWorld(key: Key): WorldMock = WorldMock(WorldCreator(NamespacedKey(key.namespace(), key.value())))
