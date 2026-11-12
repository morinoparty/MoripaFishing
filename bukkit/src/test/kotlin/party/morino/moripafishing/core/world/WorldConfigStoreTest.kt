package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.config.climate.TimePolicy
import party.morino.moripafishing.api.config.climate.WeatherPolicy
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import java.io.File
import java.util.Locale
import java.util.logging.Logger

class WorldConfigStoreTest {
    @TempDir
    lateinit var directory: File

    private val store by lazy { WorldConfigStore(directory, Logger.getLogger("WorldConfigStoreTest")) }

    @Test
    fun `round trips every climate variant and an unmanaged border`() {
        val configs =
            listOf(
                FishingWorldConfig(FishingWorldId("a"), name = mapOf(Locale.JAPAN to "えー"), border = null),
                FishingWorldConfig(
                    FishingWorldId("b"),
                    climate =
                        ClimateConfig(
                            WeatherPolicy.External(Key.key("myplugin", "realistic")),
                            TimePolicy.Fixed(6),
                        ),
                ),
                FishingWorldConfig(
                    FishingWorldId("c"),
                    climate = ClimateConfig(WeatherPolicy.Vanilla, TimePolicy.Vanilla),
                ),
                FishingWorldConfig(
                    FishingWorldId("d"),
                    climate = ClimateConfig(WeatherPolicy.Fixed(WeatherType.RAINY)),
                ),
            )
        configs.forEach(store::save)
        assertEquals(configs, store.loadAll())
    }

    @Test
    fun `an unmanaged border is written as null`() {
        store.save(FishingWorldConfig(FishingWorldId("unmanaged"), border = null))
        val text = File(directory, "unmanaged.json").readText()
        assert(Regex("\"border\"\\s*:\\s*null").containsMatchIn(text)) { text }
    }

    @Test
    fun `reads the documented format`() {
        File(directory, "default.json").writeText(
            """
            { "id": "default", "name": {"ja_JP": "つりとぴあ"}, "generator": "void",
              "spawn": {"x": 0.0, "y": 64.0, "z": 0.0, "yaw": 90.0, "pitch": 0.0},
              "border": {"centerX": 0.0, "centerZ": 0.0, "size": 100.0},
              "climate": {
                "weather": {"type": "external", "source": "myplugin:realistic"},
                "time": {"type": "real_time"}
              } }
            """.trimIndent(),
        )
        val config = store.loadAll().single()
        assertEquals(FishingWorldId("default"), config.id)
        assertEquals(WeatherPolicy.External(Key.key("myplugin", "realistic")), config.climate?.weather)
        assertEquals(TimePolicy.RealTime(), config.climate?.time)
    }

    @Test
    fun `skips invalid files`() {
        File(directory, "broken.json").writeText("{ not json")
        File(directory, "mismatch.json").writeText("""{ "id": "other" }""")
        File(directory, "Bad Id.json").writeText("""{ "id": "Bad Id" }""")
        File(directory, "badborder.json").writeText("""{ "id": "badborder", "border": {"size": -1.0} }""")
        File(directory, "badkey.json").writeText(
            """{ "id": "badkey", "climate": {"weather": {"type": "external", "source": "Bad Key"}} }""",
        )
        File(directory, "ok.json").writeText("""{ "id": "ok" }""")
        assertEquals(listOf(FishingWorldId("ok")), store.loadAll().map { it.id })
    }

    @Test
    fun `delete removes the file`() {
        store.save(FishingWorldConfig(FishingWorldId("gone")))
        store.delete(FishingWorldId("gone"))
        assertFalse(File(directory, "gone.json").exists())
    }
}
