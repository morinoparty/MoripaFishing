package party.morino.moripafishing.addons.catchannounce

import kotlinx.serialization.Serializable

/**
 * 釣果通知 Addon の設定 (`config.yml`)。
 *
 * @property messageFormat 通知メッセージのテンプレート。MiniMessage タグに加えて
 *   `<angler>`, `<fish_name>`, `<rarity_name>`, `<size>`, `<world_name>`, `<timestamp>` の
 *   プレースホルダーを利用できる。`fish_name` / `rarity_name` / `world_name` は
 *   サーバーにグローバル登録されている MoripaFishing 本体の翻訳キーを介して解決されるため、
 *   閲覧者ごとのロケールで表示される。
 * @property maxRarityWeight レアリティの重みによる通知フィルタ。`null` の場合は全ての釣果を通知する。
 *   値を設定した場合、釣れた魚の [party.morino.moripafishing.api.model.rarity.RarityData.weight] が
 *   この値以下のときのみ通知する ([party.morino.moripafishing.api.model.rarity.RarityData.weight] は
 *   値が小さいほどレアであることを表す)。
 * @property broadcastTarget 通知の配信先
 * @property broadcastPermission [broadcastTarget] が [BroadcastTarget.PERMISSION] のときにのみ使用される権限ノード
 */
@Serializable
data class CatchAnnounceConfig(
    val messageFormat: String =
        "<gray>[<gold>釣果</gold>]</gray> <angler> が <fish_name> <gray>(<rarity_name>, <size>cm)</gray> を釣り上げました",
    val maxRarityWeight: Double? = null,
    val broadcastTarget: BroadcastTarget = BroadcastTarget.ALL,
    val broadcastPermission: String = "moripafishing.addon.catchannounce.receive",
)
