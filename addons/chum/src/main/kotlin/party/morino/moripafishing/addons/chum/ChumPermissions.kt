package party.morino.moripafishing.addons.chum

/**
 * 撒き餌 Addon の権限ノード。`paper-plugin.yml` (build.gradle.kts) の宣言と揃える。
 */
object ChumPermissions {
    /** 撒き餌アイテムを使う (既定: 全員) */
    const val USE = "moripafishing.addon.chum.use"

    /** `/chum give` */
    const val GIVE = "moripafishing.addon.chum.give"

    /** `/chum odds` (自分) */
    const val ODDS = "moripafishing.addon.chum.odds"

    /** `/chum odds <player>` (他のプレイヤー) */
    const val ODDS_OTHERS = "moripafishing.addon.chum.odds.others"

    /** `/chum reload` */
    const val RELOAD = "moripafishing.addon.chum.reload"
}
