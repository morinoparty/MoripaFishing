package party.morino.moripafishing.integrations.worldlifecycle

import kotlinx.serialization.Serializable

/**
 * 参加時テレポート機能の設定。
 *
 * この機能はもともとコア (`MoripaFishing`) の `PlayerJoinListener` として実装されていたが、
 * バニラや他のスポーン系プラグインとの共存性を高めるため、本 Integration に切り出した。
 * テレポート先のスポーン地点はコアの公開 API
 * (`WorldManager.getWorld(id).getWorldDetails().spawnLocation`) から取得する。
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
