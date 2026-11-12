package party.morino.moripafishing.core.world

import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
import party.morino.moripafishing.api.config.world.WorldDetailConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.utils.Utils

/**
 * Integration 未導入時に、コアが Bukkit ワールドの状態 (ゲームルール・時刻・スポーン) を
 * 変更しないことを確認するテスト。
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@ExtendWith(MoripaFishingTest::class)
class CoreWorldPurityTest : KoinTest {
    private val plugin: MoripaFishing by inject()
    private val pluginDirectory: PluginDirectory by inject()

    private val worldId = FishingWorldId("core_purity_test")
    private lateinit var world: WorldMock
    private lateinit var configSnapshot: String

    @BeforeEach
    fun setUp() {
        // テスト専用のワールド設定ファイルを作成し、終了時に削除する
        val config =
            WorldDetailConfig(
                id = worldId,
                name = mapOf(java.util.Locale.JAPAN to worldId.value),
                spawnLocation = LocationData(worldId, 0.0, 64.0, 0.0, 90.0, 0.0),
            )
        configSnapshot = Utils.json.encodeToString(WorldDetailConfig.serializer(), config)
        configFile().writeText(configSnapshot)
        world = WorldMock(WorldCreator(NamespacedKey.fromString(worldId.toWorldKey().asString())!!))
        MoripaFishingTest.server.addWorld(world)
        // 旧実装であれば上書きしていた値と異なる値を事前に設定しておく
        // (既定の気候設定では DO_DAYLIGHT_CYCLE = false / DO_WEATHER_CYCLE = false が書き込まれていた)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true)
        world.time = 1234L
        world.spawnLocation = Location(world, 100.0, 80.0, -100.0)
    }

    @AfterEach
    fun tearDown() {
        MoripaFishingTest.server.unloadWorld(world, false)
        configFile().delete()
    }

    private fun configFile() = pluginDirectory.getWorldDirectory().resolve("${worldId.value}.json")

    private fun tick() {
        MoripaFishingTest.server.scheduler.performTicks(2)
    }

    @Test
    fun `integrations are not installed in the test environment`() {
        assertNull(plugin.getWorldLifecycleProvider())
        assertNull(plugin.getWeatherControlProvider())
    }

    @Test
    fun `updateState does not modify gamerules, time or spawn without integrations`() {
        val fishingWorld = FishingWorldImpl(worldId)
        fishingWorld.updateState()
        fishingWorld.synchronizeTime()
        tick()

        assertEquals(true, world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))
        assertEquals(true, world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE))
        // DO_DAYLIGHT_CYCLE = true のままなので、tick の分だけ自然に進む以外は変化しない
        assertTrue(world.time in 1234L..1240L, "world time was overwritten: ${world.time}")
        assertEquals(100.0, world.spawnLocation.x)
        assertEquals(80.0, world.spawnLocation.y)
        assertEquals(-100.0, world.spawnLocation.z)
    }

    @Test
    fun `setWorldSpawnPosition does not modify the bukkit spawn without integrations`() {
        val fishingWorld = FishingWorldImpl(worldId)
        // 設定値と同じ位置を渡し、設定ファイルへの書き込みが発生しないようにする
        fishingWorld.setWorldSpawnPosition(fishingWorld.getWorldSpawnPosition())
        tick()

        assertEquals(100.0, world.spawnLocation.x)
        assertEquals(80.0, world.spawnLocation.y)
        assertEquals(-100.0, world.spawnLocation.z)
    }

    @Test
    fun `deleteWorld fails without the WorldLifecycle integration and keeps the world loaded`() {
        val worldManager = WorldManagerImpl()
        assertFalse(worldManager.deleteWorld(worldId))
        assertNotNull(MoripaFishingTest.server.getWorld(worldId.toWorldKey()))
        assertEquals(configSnapshot, configFile().readText())
    }
}
