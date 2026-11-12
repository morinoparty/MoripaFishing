package party.morino.moripafishing.mocks.world

import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.core.world.WorldConfigStore
import party.morino.moripafishing.core.world.WorldKeys
import java.util.logging.Logger

/**
 * ワールド設定ディレクトリの設定ファイルから定義済みワールドを決める [WorldManager] のモック。
 * 変更系の操作はすべて失敗 (`false`) を返す。
 */
class WorldManagerMock :
    WorldManager,
    KoinComponent {
    private val configManager: ConfigManager by inject()
    private val pluginDirectory: PluginDirectory by inject()

    private var worlds: Map<FishingWorldId, FishingWorldMock> = loadWorlds()

    private fun loadWorlds(): Map<FishingWorldId, FishingWorldMock> =
        WorldConfigStore(pluginDirectory.getWorldDirectory(), Logger.getLogger("WorldManagerMock"))
            .loadAll()
            .sortedBy { it.id.value }
            .associate { it.id to FishingWorldMock(it.id, config = it) }

    override fun getDefaultWorldId(): FishingWorldId = configManager.getConfig().world.defaultId

    override fun getWorlds(): List<FishingWorld> = worlds.values.toList()

    override fun getWorld(id: FishingWorldId): FishingWorld? = worlds[id]

    override fun getWorldByKey(worldKey: Key): FishingWorld? =
        worlds.values.firstOrNull { it.getWorldKey() == WorldKeys.normalize(worldKey) }

    override fun getGeneratorIds(): Set<String> = emptySet()

    override fun createWorld(
        id: FishingWorldId,
        generatorId: String,
    ): Boolean = false

    override fun deleteWorld(id: FishingWorldId): Boolean = false

    override fun updateWorldConfig(config: FishingWorldConfig): Boolean = false

    override fun reload() {
        worlds = loadWorlds()
    }
}
