package party.morino.moripafishing.api.core.world

import party.morino.moripafishing.api.config.world.WorldDetailConfig
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


    /**
     * ワールドの詳細を取得する
     * @param fishingWorldId ワールドID
     * @return ワールドの詳細
     */
    fun getWorldDetails(fishingWorldId: FishingWorldId) : WorldDetailConfig?


    /**
     * ワールドを取得する
     * @param fishingWorldId ワールドID
     * @return ワールド
     */
    fun getWorld(fishingWorldId: FishingWorldId): FishingWorld


    /**
     * ワールドを作成する
     * @param fishingWorldId ワールドID
     * @return ワールド
     */
    fun createWorld(fishingWorldId: FishingWorldId): Boolean

    /**
     * ワールドを作成する
     * @param fishingWorldId ワールドID
     * @return ワールド
     */
    fun createWorld(fishingWorldId: FishingWorldId, generator: String?, biome: String?):Boolean

    

    /**
     * ワールドを削除する
     * @param fishingWorldId ワールドID
     */
    fun deleteWorld(fishingWorldId: FishingWorldId): Boolean



}