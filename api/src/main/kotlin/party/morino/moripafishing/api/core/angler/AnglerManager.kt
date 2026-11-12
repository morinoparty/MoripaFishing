package party.morino.moripafishing.api.core.angler

import party.morino.moripafishing.api.model.angler.AnglerId

/**
 * 釣り人を取得する。
 */
interface AnglerManager {
    /**
     * 任意の ID の釣り人を返す。存在確認はしない。
     *
     * @param id 釣り人の ID
     */
    fun getAngler(id: AnglerId): Angler

    /**
     * オンラインの全プレイヤーを釣り人として返す。
     */
    fun getOnlineAnglers(): List<Angler>
}
