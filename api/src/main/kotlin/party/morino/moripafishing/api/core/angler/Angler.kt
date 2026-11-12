package party.morino.moripafishing.api.core.angler

import net.kyori.adventure.audience.Audience
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.LocationData

/**
 * 釣り人。プレイヤーがオフラインでも取得できる。
 */
interface Angler {
    /**
     * 釣り人の ID (プレイヤーの UUID) を返す。
     */
    fun getId(): AnglerId

    /**
     * 釣り人の名前を返す。オフラインでも取得できる。一度も参加していない場合は UUID 文字列。
     */
    fun getName(): String

    /**
     * 釣り人が現在いる釣りワールドを返す。オフライン、または釣りワールド外なら `null`。
     */
    fun getWorld(): FishingWorld?

    /**
     * 釣り人の現在位置を返す。オフライン、または釣りワールド外なら `null`。
     */
    fun getLocation(): LocationData?

    /**
     * メッセージ等を送るための `Audience` を返す。オンラインならプレイヤー、オフラインなら `Audience.empty()`。
     */
    fun getAudience(): Audience
}
