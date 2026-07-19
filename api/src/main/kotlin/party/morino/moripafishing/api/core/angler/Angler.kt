package party.morino.moripafishing.api.core.angler

import net.kyori.adventure.audience.Audience
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.LocationData
import java.util.UUID

/**
 * 釣り人を表すインターフェース
 * プレイヤーの釣りデータを管理するメソッドを提供します
 */
interface Angler {
    /**
     * 釣り人のIDを取得する
     * @return 釣り人のID(これはminecraftのuuidとは一致しません)
     */
    fun getAnglerUniqueId(): AnglerId

    /**
     * 釣りびとのminecraftのuuidを取得する
     * @return 釣りびとのminecraftのuuid
     */
    fun getMinecraftUniqueId(): UUID

    /**
     * 釣り人の名前を取得する
     * @return 釣り人の名前
     */
    fun getName(): String

    /**
     * 釣り人が現在いる釣りワールドを取得する
     * @return 釣りワールド(オフライン、または釣りワールド以外にいる場合はnull)
     */
    fun getWorld(): FishingWorld?

    /**
     *  釣り人の現在の位置を取得する
     *  @return 釣り人の現在の位置(オフラインプレイヤーの場合はnull)
     */
    fun getLocation(): LocationData?

    /**
     * 釣り人へメッセージ等を送るための Audience を取得する
     * @return オンラインの場合はプレイヤーの Audience、オフラインの場合は Audience.empty()
     */
    fun getAudience(): Audience
}
