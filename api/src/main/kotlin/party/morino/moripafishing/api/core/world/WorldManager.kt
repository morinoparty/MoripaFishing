package party.morino.moripafishing.api.core.world

import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 釣りワールドの管理を行うインターフェース
 */
interface WorldManager {
    /**
     * デフォルトのワールドIDを取得する
     * @return デフォルトのワールドID
     */
    fun getDefaultWorldId(): FishingWorldId

    /**
     * 登録されているワールドIDのリストを取得する
     * @return ワールドIDのリスト
     */
    fun getWorldIdList(): List<FishingWorldId>

    /**
     * 指定されたIDのワールドが登録されているか判定する
     * @param fishingWorldId ワールドID
     * @return 登録されている場合 `true`
     */
    fun hasWorld(fishingWorldId: FishingWorldId): Boolean

    /**
     * ワールドを取得する
     * @param fishingWorldId ワールドID
     * @return ワールド。未登録またはロードされていない場合は `null`
     */
    fun getWorld(fishingWorldId: FishingWorldId): FishingWorld?

    /**
     * ロード済みのワールドの一覧を取得する
     * @return ワールドのリスト
     */
    fun getWorlds(): List<FishingWorld>

    /**
     * ワールドを作成する。
     *
     * カスタムジェネレータでの作成は `MoripaFishing-Integration-WorldLifecycle` Integration が
     * 導入されている必要がある。未導入時は `false` を返す。
     *
     * @param fishingWorldId ワールドID
     * @return 作成に成功した場合 `true`
     */
    fun createWorld(fishingWorldId: FishingWorldId): Boolean

    /**
     * 指定されたジェネレータIDでワールドを作成する。
     *
     * `MoripaFishing-Integration-WorldLifecycle` Integration が導入されている必要がある。
     * 未導入またはジェネレータIDが存在しない場合は `false` を返す。
     *
     * @param fishingWorldId ワールドID
     * @param generatorId ジェネレータID
     * @return 作成に成功した場合 `true`
     */
    fun createWorld(
        fishingWorldId: FishingWorldId,
        generatorId: String,
    ): Boolean

    /**
     * 利用可能なワールドジェネレータIDの一覧を取得する。
     * `MoripaFishing-Integration-WorldLifecycle` Integration 未導入時は空リストを返す。
     *
     * @return ジェネレータIDのリスト
     */
    fun getGeneratorIds(): List<String>

    /**
     * ワールドを削除する
     * @param fishingWorldId ワールドID
     * @return 削除に成功した場合 `true`
     */
    fun deleteWorld(fishingWorldId: FishingWorldId): Boolean

    /**
     * 登録されているすべてのワールドを初期化する
     * 存在しないワールドは作成され、すでに存在するワールドはスキップされる
     */
    fun initializeWorlds()
}
