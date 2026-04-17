package party.morino.moripafishing.api.core.world

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
     * ワールドを取得する
     * @param fishingWorldId ワールドID
     * @return ワールド
     */
    fun getWorld(fishingWorldId: FishingWorldId): FishingWorld

    /**
     * ワールドを作成する。
     *
     * カスタムジェネレータでの作成は `MoripaFishingWorldLifecycle` Integration が
     * 導入されている必要がある。未導入時は `false` を返す。
     *
     * @param fishingWorldId ワールドID
     * @return 作成に成功した場合 `true`
     */
    fun createWorld(fishingWorldId: FishingWorldId): Boolean

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
