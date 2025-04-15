package party.morino.moripafishing.api.world

import party.morino.moripafishing.api.config.WorldConfig
import party.morino.moripafishing.api.config.WorldDetailConfig
import party.morino.moripafishing.api.world.WorldId
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager

/**
 * ワールドの管理を行うクラス
 */
class WorldManager : KoinComponent {
    private val configManager: ConfigManager by inject()
    private val worldConfig : WorldConfig
        get() = configManager.getConfig().world

    /**
     * デフォルトのワールドIDを取得する
     * @return デフォルトのワールドID
     */
    fun getDefaultWorldId(): WorldId = worldConfig.defaultId

    /**
     * ワールドの詳細設定を取得する
     * @param worldId ワールドID
     * @return ワールドの詳細設定
     */
    fun getWorldDetailConfig(worldId: WorldId): WorldDetailConfig? =
        worldConfig.list.find { it.id == worldId }

    /**
     * ワールドの半径を取得する
     * @param worldId ワールドID
     * @return ワールドの半径
     */
    fun getWorldRadius(worldId: WorldId): Int =
        getWorldDetailConfig(worldId)?.radius ?: configManager.getConfig().world.defaultWorldRadius
} 