package party.morino.moripafishing.api.core.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 釣りワールドを管理する。
 *
 * 釣りワールドは設定ファイル (`world/<id>.json`) が存在すれば「定義済み」であり、
 * 対応する Bukkit ワールドがロードされているかどうかに関わらず取得できる。
 *
 * コアは Bukkit ワールドを直接変更しない。ワールドの作成・アンロードは `WorldLifecycle`、
 * 天候・時刻・ボーダー・スポーンの適用は `WorldStateApplier` を実装した Integration が行う。
 *
 * 取得系のメソッドは任意のスレッドから呼べる。変更系のメソッドはメインスレッドから呼ぶこと
 * (それ以外から呼ぶと `IllegalStateException` を投げる)。
 *
 * 釣りワールドの運用 (Integration への適用・ワールドの作成) はサーバーの起動完了 (`ServerLoadEvent`) 後に始まる。
 * それまでは定義の読み書きのみ行う。
 */
interface WorldManager {
    /**
     * デフォルトのワールド ID を返す。
     */
    fun getDefaultWorldId(): FishingWorldId

    /**
     * 定義済みの全ワールドを ID 順で返す。
     */
    fun getWorlds(): List<FishingWorld>

    /**
     * 定義済みのワールドを返す。未定義の場合は `null`。
     *
     * @param id ワールド ID
     */
    fun getWorld(id: FishingWorldId): FishingWorld?

    /**
     * Bukkit ワールドのキーから定義済みの釣りワールドを返す。該当しない場合は `null`。
     * Bukkit ワールドから釣りワールドを引く唯一の窓口。
     *
     * @param worldKey Bukkit ワールドのキー
     */
    fun getWorldByKey(worldKey: Key): FishingWorld?

    /**
     * 利用可能なジェネレーター ID を返す。`WorldLifecycle` が未登録の場合は空。
     */
    fun getGeneratorIds(): Set<String>

    /**
     * 釣りワールドを定義し、Bukkit ワールドを作成する。メインスレッドから呼ぶこと。
     *
     * `WorldLifecycle` が必要。運用開始前 (`ServerLoadEvent` 前)・既に定義済み・不正な ID・
     * 同じキーの Bukkit ワールドがロード済み・ジェネレーターが不明・作成失敗の場合は何も残さず `false` を返す。
     * 成功時は `FishingWorldLoadEvent` の後に `FishingWorldCreateEvent` を発火する。
     *
     * @param id ワールド ID
     * @param generatorId ジェネレーター ID
     * @return 作成に成功した場合 `true`
     */
    fun createWorld(
        id: FishingWorldId,
        generatorId: String,
    ): Boolean

    /**
     * 釣りワールドの定義を削除する。メインスレッドから呼ぶこと。
     *
     * Bukkit ワールドがロード済みの場合は `WorldLifecycle` によるアンロードが必要で、
     * 未登録またはアンロード失敗時は定義を残して `false` を返す。
     * 未ロードのワールドは Integration なしで削除できる。ワールドフォルダは削除しない。
     *
     * @param id ワールド ID
     * @return 削除に成功した場合 `true`
     */
    fun deleteWorld(id: FishingWorldId): Boolean

    /**
     * `config.id` のワールドの設定を保存し、即座に適用させる。メインスレッドから呼ぶこと。
     *
     * 運用開始前は保存のみ行い、適用は運用開始時に行われる。
     * `generator` は作成時のみ参照されるため、既存ワールドで変更しても効果はない。
     *
     * @param config 新しい設定
     * @return 未定義のワールドの場合 `false`
     */
    fun updateWorldConfig(config: FishingWorldConfig): Boolean

    /**
     * `world/` 配下の設定ファイルを読み直す。メインスレッドから呼ぶこと。
     *
     * ファイルが消えたワールドは定義から外すのみで、Bukkit ワールドのアンロードやイベントの発火は行わない。
     * 運用開始後は、未ロードの定義済みワールドの作成を再試行し、全ワールドの状態を即座に適用させる。
     */
    fun reload()
}
