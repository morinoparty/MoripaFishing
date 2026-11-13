package party.morino.moripafishing.addons.chum.effect

import party.morino.moripafishing.addons.chum.config.ChumConfig
import party.morino.moripafishing.addons.chum.config.ChumDefinition
import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.core.fish.FishDrawPool
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.LocationData
import java.util.logging.Logger

/**
 * 有効な撒き餌の効果を [FishDrawPool] に適用する。`AnglerFishDrawEvent` のリスナーと `/chum odds` で共有する。
 *
 * 適用は次の順に行う。
 *
 * 1. `fishWeights`: 魚の重みを設定する。複数の撒き餌が同じ魚に指定した場合は最大値を使う
 * 2. `rarityMultipliers`: レアリティの重みに、全ての撒き餌の倍率の積を掛ける
 * 3. `fishMultipliers`: 魚が釣れる全体の確率に、全ての撒き餌の倍率の積を掛ける (候補の魚の順に適用する)
 *
 * 倍率の積は、レアリティごと・魚ごとに [ChumConfig.maxMultiplier] で頭打ちにする。
 * `fishMultipliers` で魚の確率が 1 に達すると他の魚の重みは 0 になり、後から適用する魚の倍率は効かない。
 * この場合は魚ごとに 1 度だけ警告する。
 * 未登録の魚・レアリティの ID は、ID ごとに 1 度だけ警告して無視する。
 * 登録済みでも条件 (天候・ワールド) により候補でない魚・レアリティは、警告せずに無視する。
 *
 * @param store 有効な効果の参照先
 * @param config 現在の設定
 * @param api 魚・レアリティが登録済みかの確認に使う
 * @param logger 警告の出力先
 */
class ChumEffectApplier(
    private val store: ChumEffectStore,
    private val config: () -> ChumConfig,
    private val api: MoripaFishingAPI,
    private val logger: Logger,
) {
    private val warned = HashSet<String>()

    /**
     * 抽選に掛かる効果を [pool] に適用する。
     *
     * @param angler 釣りをしたつりびと
     * @param location 釣り針の位置
     * @param pool 抽選の候補
     * @return 適用した効果。設定から削除された撒き餌の効果は含まない
     */
    fun apply(
        angler: AnglerId,
        location: LocationData,
        pool: FishDrawPool,
    ): List<ActiveChum> {
        val chums = config().chums
        val effects = store.active(angler, location).filter { it.chumId in chums }
        apply(pool, effects.associate { it.chumId to chums.getValue(it.chumId) })
        return effects
    }

    /**
     * 撒き餌の定義を [pool] に適用する。
     *
     * @param pool 抽選の候補
     * @param chums 適用する撒き餌の ID と定義
     */
    fun apply(
        pool: FishDrawPool,
        chums: Map<String, ChumDefinition>,
    ) {
        if (chums.isEmpty()) return
        val max = config().maxMultiplier
        val fishWeights = LinkedHashMap<FishId, Double>()
        val rarityFactors = LinkedHashMap<RarityId, Double>()
        val fishFactors = LinkedHashMap<FishId, Double>()
        chums.forEach { (chumId, chum) ->
            chum.fishWeights.forEach { (id, weight) ->
                if (isKnownFish(chumId, id)) fishWeights.merge(FishId(id), weight) { a, b -> maxOf(a, b) }
            }
            chum.rarityMultipliers.forEach { (id, factor) ->
                if (isKnownRarity(chumId, id)) rarityFactors.merge(RarityId(id), factor) { a, b -> product(a, b) }
            }
            chum.fishMultipliers.forEach { (id, factor) ->
                if (isKnownFish(chumId, id)) fishFactors.merge(FishId(id), factor) { a, b -> product(a, b) }
            }
        }
        fishWeights.forEach { (id, weight) -> pool.setFishWeight(id, weight) }
        rarityFactors.forEach { (id, factor) -> pool.multiplyRarityWeight(id, factor.coerceAtMost(max)) }
        applyFishFactors(pool, fishFactors, max)
    }

    private fun applyFishFactors(
        pool: FishDrawPool,
        fishFactors: Map<FishId, Double>,
        max: Double,
    ) {
        pool.getFishes().forEach { fish ->
            val factor = fishFactors[fish.id] ?: return@forEach
            val before = pool.getProbability(fish.id)
            pool.multiplyFishProbability(fish.id, factor.coerceAtMost(max))
            if (before < 1.0 && pool.getProbability(fish.id) >= 1.0) warnSaturated(fish.id)
        }
    }

    /** 警告履歴を消す。設定の再読み込み時に呼ぶ。 */
    fun clearWarnings() {
        warned.clear()
    }

    private fun isKnownFish(
        chumId: String,
        id: String,
    ): Boolean {
        if (api.getFishManager().get(FishId(id)) != null) return true
        warnOnce("fish", chumId, id)
        return false
    }

    private fun isKnownRarity(
        chumId: String,
        id: String,
    ): Boolean {
        if (api.getRarityManager().get(RarityId(id)) != null) return true
        warnOnce("rarity", chumId, id)
        return false
    }

    private fun warnOnce(
        kind: String,
        chumId: String,
        id: String,
    ) {
        if (warned.add("$kind:$id")) logger.warning("Chum '$chumId' refers to unknown $kind '$id'; ignoring it")
    }

    private fun warnSaturated(fish: FishId) {
        if (warned.add("saturated:${fish.value}")) {
            logger.warning("Chum fish multiplier made '${fish.value}' the only drawable fish; multipliers for other fish have no effect")
        }
    }

    private companion object {
        /** 有限の倍率の積。溢れた場合は [Double.MAX_VALUE] に丸める (無限大と 0 の積で NaN にならないように)。 */
        fun product(
            a: Double,
            b: Double,
        ): Double = (a * b).coerceAtMost(Double.MAX_VALUE)
    }
}
