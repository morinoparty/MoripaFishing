package party.morino.moripafishing.addons.catchannounce

import kotlinx.serialization.Serializable

/**
 * 釣果を誰に通知するかを表す。
 */
@Serializable
enum class BroadcastTarget {
    /** サーバー全体に通知する */
    ALL,

    /** 釣り上げたワールドに滞在している釣り人にのみ通知する */
    SAME_WORLD,

    /** [CatchAnnounceConfig.broadcastPermission] を持つプレイヤーにのみ通知する */
    PERMISSION,
}

/**
 * 釣果通知 Addon の設定。
 *
 * @property enabled 通知機能自体の有効/無効
 * @property messageFormat 通知メッセージのテンプレート。MiniMessage タグに加えて
 *   `<angler>`, `<fish_name>`, `<rarity_name>`, `<size>`, `<world_name>`, `<timestamp>` の
 *   プレースホルダーを利用できる。`fish_name` / `rarity_name` / `world_name` は
 *   サーバーにグローバル登録されている MoripaFishing 本体の翻訳キーを介して解決されるため、
 *   閲覧者ごとのロケールで表示される。
 * @property minRarityWeight レアリティの重みによる通知フィルタ。`null` の場合は全ての釣果を通知する。
 *   値を設定した場合、釣れた魚の [party.morino.moripafishing.api.model.rarity.RarityData.weight] が
 *   この値以下のときのみ通知する ([party.morino.moripafishing.api.model.rarity.RarityData.weight] は
 *   値が小さいほどレアであることを表す)。
 * @property broadcastTarget 通知の配信先
 * @property broadcastPermission [broadcastTarget] が [BroadcastTarget.PERMISSION] のときにのみ使用される権限ノード
 */
@Serializable
data class CatchAnnounceConfig(
    val enabled: Boolean = true,
    val messageFormat: String =
        "<gray>[<gold>釣果</gold>]</gray> <angler> が <fish_name> <gray>(<rarity_name>, <size>cm)</gray> を釣り上げたのだ！",
    val minRarityWeight: Double? = null,
    val broadcastTarget: BroadcastTarget = BroadcastTarget.ALL,
    val broadcastPermission: String = "moripafishing.addon.catchannounce.receive",
)
