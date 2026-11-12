package party.morino.moripafishing.api

import org.bukkit.Bukkit

/**
 * [MoripaFishingAPI] の取得口。
 *
 * Bukkit の `ServicesManager` に登録された API を返す薄いファサードで、状態を持たない。
 * コア (`MoripaFishing`) は有効化時に API を登録し、無効化時に登録を解除する。
 */
object MoripaFishingAPIProvider {
    /**
     * API を取得する。
     *
     * @throws IllegalStateException コアが有効でない場合
     */
    @JvmStatic
    fun get(): MoripaFishingAPI = getOrNull() ?: error("MoripaFishingAPI is not available. Is the MoripaFishing plugin enabled?")

    /**
     * API を取得する。コアが有効でない場合は `null` を返す。
     */
    @JvmStatic
    fun getOrNull(): MoripaFishingAPI? = Bukkit.getServicesManager().load(MoripaFishingAPI::class.java)
}
