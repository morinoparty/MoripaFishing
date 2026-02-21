package party.morino.moripafishing.api.config

import kotlinx.serialization.Serializable

/**
 * プラグインのサブ機能の有効/無効を管理する設定
 *
 * コア機能（カスタム魚の生成・釣り上げイベント処理）以外の機能をここで切り替える。
 * グローバルリスナーに関する設定であり、変更はサーバー再起動時に反映される。
 *
 * @property enableTeleportOnJoin プレイヤー参加時にデフォルト釣りワールドのスポーン地点へ
 *   自動テレポートするかどうか。`false` にするとテレポートを行わず、バニラの参加動作に委ねる。
 * @property enableCatchAnnouncements 魚を釣り上げた際に、サーバー全体に釣果をブロードキャストするかどうか。
 *   `false` にするとアナウンスを行わない。
 */
@Serializable
data class FeaturesConfig(
    val enableTeleportOnJoin: Boolean = true,
    val enableCatchAnnouncements: Boolean = true,
)
