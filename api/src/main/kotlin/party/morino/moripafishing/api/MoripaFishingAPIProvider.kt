package party.morino.moripafishing.api

/**
 * [MoripaFishingAPI] の安定した取得口。
 *
 * コア (`MoripaFishing`) が有効化時に [register] し、無効化時に [unregister] する。
 * 外部プラグインはプラグイン名でのキャストに頼らず、ここから API を取得できる。
 * Bukkit の `ServicesManager` にも同じインスタンスが登録される。
 */
object MoripaFishingAPIProvider {
    @Volatile
    private var instance: MoripaFishingAPI? = null

    /**
     * API を取得する。
     * @throws IllegalStateException コアが未初期化（未導入・無効化済み）の場合
     */
    fun get(): MoripaFishingAPI =
        instance
            ?: throw IllegalStateException("MoripaFishingAPI is not available. Is the MoripaFishing plugin enabled?")

    /**
     * API を取得する。コアが未初期化の場合は `null` を返す。
     */
    fun getOrNull(): MoripaFishingAPI? = instance

    /**
     * API を登録する。コアの有効化時に呼ばれる内部用メソッド。
     * コア以外のプラグインが呼び出してはならない。
     * @throws IllegalStateException 別のインスタンスが登録済みの場合
     */
    fun register(api: MoripaFishingAPI) {
        val current = instance
        check(current == null || current === api) {
            "MoripaFishingAPI is already registered by another instance."
        }
        instance = api
    }

    /**
     * API の登録を解除する。コアの無効化時に呼ばれる内部用メソッド。
     */
    fun unregister() {
        instance = null
    }
}
