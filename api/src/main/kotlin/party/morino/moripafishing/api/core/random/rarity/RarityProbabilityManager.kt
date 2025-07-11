package party.morino.moripafishing.api.core.random.rarity

import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Spot

/**
 * レアリティの確率を動的に管理するマネージャーインターフェース
 * 設定値に加えて、動的に確率を調整できる機能を提供する
 */
interface RarityProbabilityManager {
    /**
     * レアリティの確率にSpot単位で修正値を適用する
     *
     * @param spot 対象のスポット
     * @param rarityId 対象のレアリティ
     * @param applyValue 適用値（ADD: 加算, MULTIPLY: 乗算, CONSTANT: 固定値）
     * @param limit 有効期限（ミリ秒）、nullの場合は無期限
     */
    fun applyRarityModifierForSpot(
        spot: Spot,
        rarityId: RarityId,
        applyValue: ApplyValue,
        limit: Long? = null,
    )

    /**
     * レアリティの確率にAngler単位で修正値を適用する
     *
     * @param anglerId 対象の釣り人ID
     * @param rarityId 対象のレアリティ
     * @param applyValue 適用値
     * @param limit 有効期限（ミリ秒）、nullの場合は無期限
     */
    fun applyRarityModifierForAngler(
        anglerId: AnglerId,
        rarityId: RarityId,
        applyValue: ApplyValue,
        limit: Long? = null,
    )

    /**
     * レアリティの確率にWorld単位で修正値を適用する
     *
     * @param worldId 対象のワールドID
     * @param rarityId 対象のレアリティ
     * @param applyValue 適用値
     * @param limit 有効期限（ミリ秒）、nullの場合は無期限
     */
    fun applyRarityModifierForWorld(
        worldId: FishingWorldId,
        rarityId: RarityId,
        applyValue: ApplyValue,
        limit: Long? = null,
    )

    /**
     * レアリティの基本重みを設定する
     *
     * @param rarityId 対象のレアリティ
     * @param baseWeight 基本重み
     */
    fun setBaseRarityWeight(
        rarityId: RarityId,
        baseWeight: Double,
    )

    /**
     * レアリティの基本重みを取得する
     *
     * @param rarityId 対象のレアリティ
     * @return 基本重み（設定されていない場合は1.0）
     */
    fun getBaseRarityWeight(rarityId: RarityId): Double

    /**
     * 指定された釣り人とコンテキストに対するレアリティの修正後確率を取得する
     *
     * @param angler 対象の釣り人
     * @param rarityId 対象のレアリティ
     * @return 修正後の重み
     */
    fun getModifiedRarityWeight(
        angler: Angler,
        rarityId: RarityId,
    ): Double

    /**
     * 特定の釣り人に適用されているレアリティ修正値をクリアする
     *
     * @param anglerId 対象の釣り人ID
     */
    fun clearAnglerRarityModifiers(anglerId: AnglerId)

    /**
     * 期限切れのレアリティ修正値をクリーンアップする
     */
    fun cleanupExpiredRarityModifiers()
}
