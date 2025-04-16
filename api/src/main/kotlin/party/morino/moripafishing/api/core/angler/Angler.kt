package party.morino.moripafishing.api.core.angler

import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 釣り人を表すインターフェース
 * プレイヤーの釣りデータを管理するメソッドを提供します
 */
interface Angler {
    /**
     * 釣り人のIDを取得する
     * @return 釣り人のID
     */
    fun getId(): AnglerId


    /**
     * 釣り人が釣った魚を記録する
     * @param fish 釣った魚のID
     * @return 更新された釣り人のデータ
     */
    fun recordCaughtFish(fish: Fish)
    
    /**
     * 釣り人のデータを取得する
     * @return 釣り人のデータ(オフラインプレイヤーの場合はnull)
     */
    fun getWorld() : FishingWorldId?
}