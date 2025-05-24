package party.morino.moripafishing.api.core.fishing

import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Spot

/**
 * ルアータイム（魚がかかるまでの時間）を管理するインターフェース
 * プレイヤーやワールド、ロケーションごとにルアータイムを制御できる
 */
interface WaitTimeManager {

    /**
     * スポット単位でルアータイムを適用
     * @param spot スポット
     * @param applyValue 適用値
     * @param limit 有効期限(ミリ秒)
     */
    fun applyForSpot(
        spot: Spot,
        applyValue: ApplyValue,
        limit: Long?,
    )

    /**
     * プレイヤー単位でルアータイムを適用
     * @param anglerId プレイヤーID
     * @param applyValue 適用値
     * @param limit 有効期限(ミリ秒)
     */
    fun applyForAngler(
        anglerId: AnglerId,
        applyValue: ApplyValue,
        limit: Long?,
    )

    /**
     * ワールド単位でルアータイムを適用
     * @param worldId ワールドID
     * @param applyValue 適用値
     * @param limit 有効期限(ミリ秒)
     */
    fun applyForWorld(
        worldId: FishingWorldId,
        applyValue: ApplyValue,
        limit: Long?,
    )

    /**
     * プレイヤー単位でルアータイムを取得
     * @param angler プレイヤー
     * @return ルアータイム(秒)
     */
    fun getWaitTime(angler: Angler): Pair<Int, Int>
}
