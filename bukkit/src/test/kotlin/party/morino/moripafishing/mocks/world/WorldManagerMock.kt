package party.morino.moripafishing.mocks.world

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.config.world.WorldConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.generator.GeneratorData

/**
 * WorldManagerのモッククラス
 */
class WorldManagerMock : WorldManager, KoinComponent {
    private val configManager: ConfigManager by inject()
    private val pluginDirectory: PluginDirectory by inject()
    private val worldConfig: WorldConfig
        get() = configManager.getConfig().world

    private lateinit var worldIdList: List<FishingWorldId>

    init {
        loadWorldIds()
    }

    private fun loadWorldIds() {
        val worldDirectory = pluginDirectory.getWorldDirectory()
        if (!worldDirectory.exists()) {
            worldDirectory.mkdirs()
        }
        worldIdList = pluginDirectory.getWorldDirectory().listFiles()?.mapNotNull { file ->
            if (file.name.endsWith(".json")) {
                val worldId = file.nameWithoutExtension
                FishingWorldId(worldId)
            } else {
                null
            }
        } ?: emptyList()
    }

    /**
     * デフォルトのワールドIdを返す (モック用)
     */
    override fun getDefaultWorldId(): FishingWorldId {
        return worldConfig.defaultId
    }

    /**
     * ワールドIDのリストを返す (モック用)
     */
    override fun getWorldIdList(): List<FishingWorldId> {
        return worldIdList
    }

    /**
     * ワールドを取得する (モック用、未実装)
     */
    override fun getWorld(fishingWorldId: FishingWorldId): FishingWorld {
        return FishingWorldMock(fishingWorldId)
    }

    /**
     * ワールドを作成する (モック用、常にnull)
     */
    override fun createWorld(fishingWorldId: FishingWorldId): Boolean {
        worldIdList.plus(fishingWorldId)
        return true
    }

    override fun createWorld(fishingWorldId: FishingWorldId, generatorData: GeneratorData): Boolean {
        worldIdList.plus(fishingWorldId)
        return true
    }

    override fun deleteWorld(fishingWorldId: FishingWorldId): Boolean {
        worldIdList.minus(fishingWorldId)
        return true
    }

    override fun initializeWorlds() {
        // モックなので何もしないのだ
    }
}
