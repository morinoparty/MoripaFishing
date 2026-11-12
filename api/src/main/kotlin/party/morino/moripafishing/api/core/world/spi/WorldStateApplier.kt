package party.morino.moripafishing.api.core.world.spi

/**
 * [WorldState] を Bukkit ワールドへ適用する。
 *
 * `ServicesManager` に登録し、登録された全件が呼ばれる。各実装は自分が担当する項目のみを見ること。
 * 対象の Bukkit ワールドが未ロードの場合は何もしない。
 *
 * 型の制約: シグネチャには `:api` の型・`Key`・`String`・Java のプリミティブ・`java.util` のコレクションのみを使う。
 * 実装側も `:api` のクラスのコンストラクタを既定引数を省略して呼ばないこと (詳細は [WorldState])。
 * スレッド: [apply] はメインスレッドから呼ばれる。
 */
fun interface WorldStateApplier {
    /**
     * 状態を適用する。冪等であること。
     *
     * @param state コアが決定したワールドの状態 (全項目)
     */
    fun apply(state: WorldState)
}
