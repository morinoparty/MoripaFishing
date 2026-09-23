package party.morino.moripafishing.integrations.worldlifecycle.api

/**
 * 釣りワールドのライフサイクル（境界制御・ワールド生成）を提供する SPI。
 *
 * MoripaFishing 本体はこのプロバイダーが登録されている場合のみワールド境界の同期や
 * カスタムジェネレーターでのワールド作成を行う。プロバイダー未登録時は該当機能はスキップされる。
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
}
