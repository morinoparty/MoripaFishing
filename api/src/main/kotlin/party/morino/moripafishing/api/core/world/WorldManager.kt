package party.morino.moripafishing.api.core.world

import party.morino.moripafishing.api.config.WorldDetailConfig
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * ワールドの管理を行うクラス
 */
interface WorldManager {


    /**
     * デフォルトのワールドIdを取得する
     * @return デフォルトのワールドId
     */
    fun getDefaultWorldId(): FishingWorldId


    /**
     * ワールドのリストを取得する
     * @return ワールドのリスト
     */
    fun getWorldIdList(): List<FishingWorldId>


    fun getWorldDetails(fishingWorldId: FishingWorldId) : WorldDetailConfig?


    /**
     * ワールドを取得する
     * @param fishingWorldId ワールドID
     * @return ワールド
     */
    fun getWorld(fishingWorldId: FishingWorldId): FishingWorld

}