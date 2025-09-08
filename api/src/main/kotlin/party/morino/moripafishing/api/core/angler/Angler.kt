package party.morino.moripafishing.api.core.angler

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
     * 釣り人のデータを取得する
     * @return 釣り人のデータ(オフラインプレイヤーの場合はnull)
     */
    fun getWorld(): FishingWorld?

    /**
     *  釣り人の現在の位置を取得する
     *  @return 釣り人の現在の位置(オフラインプレイヤーの場合はnull)
     */
    fun getLocation(): LocationData?
}
