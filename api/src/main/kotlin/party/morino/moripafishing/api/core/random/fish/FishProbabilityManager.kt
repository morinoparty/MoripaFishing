package party.morino.moripafishing.api.core.random.fish

import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Spot

/**
 * 魚の確率を動的に管理するマネージャーインターフェース
 * 設定値に加えて、動的に確率を調整できる機能を提供する
 */
interface FishProbabilityManager {
    /**
     * 魚の確率にSpot単位で修正値を適用する
     *
     * @param spot 対象のスポット
     * @param fishId 対象の魚ID
     * @param applyValue 適用値（ADD: 加算, MULTIPLY: 乗算, CONSTANT: 固定値）
     * @param limit 有効期限（ミリ秒）、nullの場合は無期限
     */
    fun applyFishModifierForSpot(
        spot: Spot,
        fishId: FishId,
        applyValue: ApplyValue,
        limit: Long? = null,
    )

    /**
     * 魚の確率にAngler単位で修正値を適用する
     *
     * @param anglerId 対象の釣り人ID
     * @param fishId 対象の魚ID
     * @param applyValue 適用値
     * @param limit 有効期限（ミリ秒）、nullの場合は無期限
     */
    fun applyFishModifierForAngler(
        anglerId: AnglerId,
        fishId: FishId,
        applyValue: ApplyValue,
        limit: Long? = null,
    )

    /**
     * 魚の確率にWorld単位で修正値を適用する
     *
     * @param worldId 対象のワールドID
     * @param fishId 対象の魚ID
     * @param applyValue 適用値
     * @param limit 有効期限（ミリ秒）、nullの場合は無期限
     */
    fun applyFishModifierForWorld(
        worldId: FishingWorldId,
        fishId: FishId,
        applyValue: ApplyValue,
        limit: Long? = null,
    )

    /**
     * 魚の基本重みを設定する
     *
     * @param fishId 対象の魚ID
     * @param baseWeight 基本重み
     */
    fun setBaseFishWeight(
        fishId: FishId,
        baseWeight: Double,
    )

    /**
     * 魚の基本重みを取得する
     *
     * @param fishId 対象の魚ID
     * @return 基本重み（設定されていない場合は1.0）
     */
    fun getBaseFishWeight(fishId: FishId): Double

    /**
     * 指定された釣り人とコンテキストに対する魚の修正後確率を取得する
     *
     * @param angler 対象の釣り人
     * @param fishId 対象の魚ID
     * @return 修正後の重み
     */
    fun getModifiedFishWeight(
        angler: Angler,
        fishId: FishId,
    ): Double

    /**
     * 特定の釣り人に適用されている魚修正値をクリアする
     *
     * @param anglerId 対象の釣り人ID
     */
    fun clearAnglerFishModifiers(anglerId: AnglerId)

    /**
     * 期限切れの魚修正値をクリーンアップする
     */
    fun cleanupExpiredFishModifiers()
}
