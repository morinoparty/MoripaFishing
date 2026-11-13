package party.morino.moripafishing.integrations.worldlifecycle

import kotlinx.serialization.Serializable

/**
 * 参加時テレポート機能の設定。
 *
 * テレポート先のスポーン地点は、管理ワールドならコアの公開 API (`WorldBinding.Managed.spawn`)、
 * 外部ワールドなら Bukkit ワールドのスポーン地点から取得する。
 * 釣りワールドの Bukkit ワールドが未ロードの場合はテレポートしない。
 *
 * @property enabled 参加時テレポート機能自体の有効/無効
 * @property worldId テレポート先の釣りワールドID。`null` の場合はコアのデフォルトワールドを使用する。
 * @property onlyFirstJoin `true` の場合、そのサーバーに初めて参加したプレイヤーにのみテレポートを行う。
 */
@Serializable
data class JoinTeleportConfig(
    val enabled: Boolean = true,
    val worldId: String? = null,
    val onlyFirstJoin: Boolean = false,
)
