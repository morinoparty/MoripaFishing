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
