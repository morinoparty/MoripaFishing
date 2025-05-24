package party.morino.moripafishing.api.core.world

import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.generator.GeneratorData

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
     * @param generatorData ワールド生成データ
     * @return ワールド
     */
    fun createWorld(
        fishingWorldId: FishingWorldId,
        generatorData: GeneratorData,
    ): Boolean

    /**
     * ワールドを削除する
     * @param fishingWorldId ワールドID
     */
    fun deleteWorld(fishingWorldId: FishingWorldId): Boolean

    /**
     * 登録されているすべてのワールドを初期化する
     * 存在しないワールドは作成され、すでに存在するワールドはスキップされる
     */
    fun initializeWorlds()
}
