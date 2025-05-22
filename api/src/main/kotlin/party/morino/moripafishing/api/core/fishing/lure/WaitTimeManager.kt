package party.morino.moripafishing.api.core.fishing.lure

import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Spot
/**
 * ルアータイム（魚がかかるまでの時間）を管理するインターフェース
 * プレイヤーやワールド、ロケーションごとにルアータイムを制御できる
 */
interface WaitTimeManager {
    fun applyForSpot(spot: Spot): Long
    fun applyForAngler(anglerId: AnglerId): Long
    fun applyForWorld(worldId: FishingWorldId): Long
}
