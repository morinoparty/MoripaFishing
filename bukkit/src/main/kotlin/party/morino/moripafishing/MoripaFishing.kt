package party.morino.moripafishing

import org.bukkit.plugin.java.JavaPlugin

/**
 * MoripaFishing のプラグイン本体。処理は有効化中の [MoripaFishingCore] に委ねる。
 *
 * MockBukkit がテストでプラグインのサブクラスを生成するため `open` にしている。
 */
open class MoripaFishing : JavaPlugin() {
    private var core: MoripaFishingCore? = null

    /**
     * 有効化中のコア。コマンドから遅延参照する。
     *
     * @throws IllegalStateException プラグインが有効でない場合
     */
    internal fun core(): MoripaFishingCore = checkNotNull(core) { "MoripaFishing is not enabled" }

    override fun onEnable() {
        core = MoripaFishingCore(this).also { it.enable() }
    }

    override fun onDisable() {
        core?.disable()
        core = null
    }
}
