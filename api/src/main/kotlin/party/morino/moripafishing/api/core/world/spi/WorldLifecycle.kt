package party.morino.moripafishing.api.core.world.spi

import net.kyori.adventure.key.Key

/**
 * Bukkit ワールドの作成・アンロード。
 *
 * `ServicesManager` に登録し、最優先の登録 1 件だけが使われる。
 *
 * 型の制約: シグネチャには `:api` の型・`Key`・`String`・Java のプリミティブ・`java.util` のコレクションのみを使う。
 * 実装側も `:api` のクラスのコンストラクタを既定引数を省略して呼ばないこと (詳細は [WorldState])。
 * スレッド: [getGeneratorIds] は任意のスレッド (コマンドの補完など) から、それ以外はメインスレッドから呼ばれる。
 */
interface WorldLifecycle {
    /**
     * 利用可能なジェネレーター ID の一覧を返す。任意のスレッドから呼ばれるため、スレッドセーフに実装すること。
     */
    fun getGeneratorIds(): Set<String>

    /**
     * 指定したジェネレーターで Bukkit ワールドを作成する。
     *
     * 同じキーのワールドが既に存在する・ジェネレーターが不明・作成に失敗した場合は `false` を返す。
     * `Bukkit.createWorld` は `WorldLoadEvent` を発火する。
     *
     * @param worldKey 作成するワールドのキー
     * @param generatorId ジェネレーター ID
     * @return 作成に成功した場合 `true`
     */
    fun createWorld(
        worldKey: Key,
        generatorId: String,
    ): Boolean

    /**
     * Bukkit ワールドを保存せずにアンロードする。
     *
     * @param worldKey アンロードするワールドのキー
     * @return アンロードに成功した場合 `true`。未ロード・失敗時は `false`
     */
    fun unloadWorld(worldKey: Key): Boolean
}
