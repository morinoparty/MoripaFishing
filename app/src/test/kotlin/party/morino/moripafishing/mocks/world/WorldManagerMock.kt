package party.morino.moripafishing.mocks.world

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.WorldConfig
import party.morino.moripafishing.api.config.WorldDetailConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * WorldManagerのモッククラスなのだ
 */
class WorldManagerMock : WorldManager, KoinComponent {
    private val configManager : ConfigManager by inject()
    private val defaultWorldId = configManager
    private val worldConfig : WorldConfig
        get() = configManager.getConfig().world
    /**
     * デフォルトのワールドIdを返すのだ (モック用)
     */
    override fun getDefaultWorldId(): FishingWorldId {
        return worldConfig.defaultId
    }

    /**
     * ワールドIDのリストを返すのだ (モック用)
     */
    override fun getWorldIdList(): List<FishingWorldId> {
        return worldConfig.list.map { it.id }
    }

    /**
     * ワールドの詳細を返すのだ (モック用、常にnull)
     */
    override fun getWorldDetails(fishingWorldId: FishingWorldId): WorldDetailConfig? {
        return worldConfig.list.find { it.id == fishingWorldId }
    }

    /**
     * ワールドを取得するのだ (モック用、未実装)
     * TODO: FishingWorldのモックを返すように実装する必要があるのだ
     */
    override fun getWorld(fishingWorldId: FishingWorldId): FishingWorld {
        TODO("FishingWorldのモックを返すように実装する必要があるのだ")
    }

    /**
     * ワールドを作成するのだ (モック用、常にnull)
     */
    override fun createWorld(fishingWorldId: FishingWorldId): FishingWorld? {
        return null // モックではワールド作成をサポートしないのだ
    }
} 