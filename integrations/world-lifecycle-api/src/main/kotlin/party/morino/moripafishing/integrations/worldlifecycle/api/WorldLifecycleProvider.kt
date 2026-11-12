package party.morino.moripafishing.integrations.worldlifecycle.api

/**
 * 釣りワールドのライフサイクル（境界・時刻・スポーンの制御、ワールド生成・アンロード）を提供する SPI。
 *
 * MoripaFishing 本体はこのプロバイダーが登録されている場合のみ、ワールド境界の同期、
 * ゲームルール `DO_DAYLIGHT_CYCLE` と時刻の同期、ワールドスポーンの反映、
 * カスタムジェネレーターでのワールド作成、ワールドのアンロードを行う。
 * プロバイダー未登録時は該当機能はスキップされ、コアは Bukkit ワールドを改変しない。
 *
 * 本 SPI は **Integration** パターンとして提供され、通常は別 jar
 * (`MoripaFishing-Integration-WorldLifecycle` 等) の `JavaPlugin` がこれを実装し、
 * コア側は Bukkit の softdepend で実体を検出して利用する。
 *
 * ### 境界を跨ぐ型について
 *
 * 本 SPI は `:api` モジュールに一切依存しない。ワールドキー (`moripafishing:<worldId>`) や
 * ジェネレータ ID は `String` としてやり取りし、ジェネレータ定義は本モジュールに定義された
 * [GeneratorData] を用いる。これにより classloader の class identity 問題が
 * 発生する余地をなくしている。
 */
interface WorldLifecycleProvider {
    /**
     * 指定された釣りワールドの Bukkit `WorldBorder` を更新する。
     *
     * @param worldKey 対象 Bukkit ワールドのキー文字列 (例: `moripafishing:default`)
     * @param centerX ボーダーの中心 X 座標
     * @param centerZ ボーダーの中心 Z 座標
     * @param size ボーダーのサイズ
     */
    fun applyBorder(
        worldKey: String,
        centerX: Double,
        centerZ: Double,
        size: Double,
    )

    /**
     * 指定された生成データで Bukkit ワールドを作成する。
     *
     * 既存のワールドがある場合や作成に失敗した場合は `false` を返す。
     *
     * @param worldKey 作成する Bukkit ワールドのキー文字列 (例: `moripafishing:default`)
     * @param generatorData ジェネレータ情報
     * @return 作成に成功した場合 `true`
     */
    fun createBukkitWorld(
        worldKey: String,
        generatorData: GeneratorData,
    ): Boolean

    /**
     * ジェネレータ ID から定義を取得する。
     *
     * @param id ジェネレータ ID
     * @return 対応する定義（見つからない場合は `null`）
     */
    fun getGenerator(id: String): GeneratorData?

    /**
     * 登録されているすべてのジェネレータ定義を返す。
     */
    fun listGenerators(): List<GeneratorData>

    /**
     * 新しいジェネレータ定義を登録する。
     *
     * @param generator 追加する定義
     */
    fun addGenerator(generator: GeneratorData)

    /**
     * 指定された釣りワールドの昼夜サイクル (ゲームルール `DO_DAYLIGHT_CYCLE`) を設定する。
     *
     * コアは `ClimateConfig.enableDayCycle` が有効な場合 (時刻をコアが同期する場合) に `false`、
     * 無効な場合に `true` を渡す。
     *
     * 既定実装は何もしない。本メソッドを持たない旧版の実装との互換性のためである。
     *
     * @param worldKey 対象 Bukkit ワールドのキー文字列 (例: `moripafishing:default`)
     * @param enabled バニラの昼夜サイクルを有効にする場合 `true`
     */
    fun setDaylightCycle(
        worldKey: String,
        enabled: Boolean,
    ) {
        // 既定では何もしない
    }

    /**
     * 指定された釣りワールドの時刻を設定する。
     *
     * 時刻の計算 (固定時刻・実時刻との同期) はコアが行い、本メソッドは計算済みの値を適用する。
     *
     * 既定実装は何もしない。本メソッドを持たない旧版の実装との互換性のためである。
     *
     * @param worldKey 対象 Bukkit ワールドのキー文字列 (例: `moripafishing:default`)
     * @param time 設定するワールド時刻 (tick, `0..23999`)
     */
    fun setTime(
        worldKey: String,
        time: Long,
    ) {
        // 既定では何もしない
    }

    /**
     * 指定された釣りワールドの Bukkit ワールドスポーンを設定する。
     *
     * スポーン位置の設定ファイルへの保存はコアが行い、本メソッドは Bukkit ワールドへの反映のみを担う。
     *
     * 既定実装は何もしない。本メソッドを持たない旧版の実装との互換性のためである。
     *
     * @param worldKey 対象 Bukkit ワールドのキー文字列 (例: `moripafishing:default`)
     * @param x X 座標
     * @param y Y 座標
     * @param z Z 座標
     * @param yaw ヨー
     * @param pitch ピッチ
     */
    fun setSpawnLocation(
        worldKey: String,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
    ) {
        // 既定では何もしない
    }

    /**
     * 指定された釣りワールドをアンロードする。
     *
     * コアはワールド削除 (`WorldManager.deleteWorld`) の際に、本メソッドの結果を見て
     * 設定ファイルの削除などの後続処理を行うか判断する。そのため結果を同期的に返す必要があり、
     * **メインスレッドから呼び出すこと**。メインスレッド以外から呼ばれた場合、実装は
     * アンロードせずに `false` を返してよい。
     *
     * 既定実装はアンロードせずに `false` を返す。本メソッドを持たない旧版の実装との互換性のためである。
     *
     * @param worldKey 対象 Bukkit ワールドのキー文字列 (例: `moripafishing:default`)
     * @param save アンロード前にワールドを保存する場合 `true`
     * @return アンロードに成功した場合 `true`
     */
    fun unloadBukkitWorld(
        worldKey: String,
        save: Boolean,
    ): Boolean = false
}
