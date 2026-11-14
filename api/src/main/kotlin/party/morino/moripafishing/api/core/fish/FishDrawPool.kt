package party.morino.moripafishing.api.core.fish

import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import java.util.Collections

/**
 * 1 回の抽選の候補と、その重み。[FishDrawer.createPool] で作り、[FishDrawer.draw] に渡す。
 *
 * ## 候補 (メンバー)
 *
 * `enabled` が `true` で、レアリティが [rarities] に含まれ、[FishData.conditions] が [world] と [weather] で満たされる魚が候補になる。
 * この条件は絶対で、候補外の魚を後から加えることはできない。
 * レアリティは候補の魚を 1 匹以上持つものだけが候補になる。
 *
 * 重みの初期値は [RarityData.weight] と [FishData.weight] である。
 * `weight` が 0 の魚も重み 0 の候補として含まれるため、[setFishWeight] で重みを与えれば釣れるようになる (餌限定の魚など)。
 *
 * ## 抽選
 *
 * まず重み付きでレアリティを選び、その中から重み付きで魚を選ぶ。
 * 抽選対象になるのは、重みが正で、重みが正の魚を 1 匹以上持つレアリティだけである。
 * そのようなレアリティが無ければ抽選結果は `null` (バニラのドロップ) になる。
 *
 * ## 重みの変更
 *
 * 重みと倍率は有限かつ 0 以上でなければならず、それ以外は [IllegalArgumentException] を投げる。
 * 検証は変更の前に行うため、拒否された呼び出しは何も変更しない。
 * 結果が [Double.MAX_VALUE] を超える場合は [Double.MAX_VALUE] に丸める。
 *
 * スレッドセーフではない。1 回の抽選の中で、同じスレッドから使うことを想定している。
 *
 * @param world 抽選する釣りワールドの ID
 * @param weather 釣りワールドの現在の天候。不明なら `null` (天候条件付きの魚は候補外になる)
 * @param rarities 登録されているレアリティ
 * @param fishes 登録されている魚
 */
@Suppress("TooManyFunctions") // 抽選の候補を操作する公開 API をまとめて提供するため
class FishDrawPool(
    private val world: FishingWorldId,
    private val weather: WeatherType?,
    rarities: List<RarityData>,
    fishes: List<FishData>,
) {
    private val fishesByRarity: Map<RarityId, List<FishData>>
    private val rarities: List<RarityData>
    private val fishById: Map<FishId, FishData>
    private val rarityWeights = LinkedHashMap<RarityId, Double>()
    private val fishWeights = LinkedHashMap<FishId, Double>()

    init {
        val registered = rarities.distinctBy { it.id }
        val registeredIds = registered.mapTo(HashSet()) { it.id }
        val members =
            fishes
                .distinctBy { it.id }
                .filter { it.enabled && it.rarity in registeredIds && it.conditions.matches(world, weather) }
        // 候補は絶対なので、getter から返すリストを変更できないようにする
        fishesByRarity = members.groupBy { it.rarity }.mapValues { Collections.unmodifiableList(it.value) }
        this.rarities = Collections.unmodifiableList(registered.filter { it.id in fishesByRarity })
        fishById = members.associateBy { it.id }
        this.rarities.forEach { rarityWeights[it.id] = it.weight }
        members.forEach { fishWeights[it.id] = it.weight }
    }

    /** 抽選する釣りワールドの ID を返す。 */
    fun getWorld(): FishingWorldId = world

    /** 候補の判定に使った天候を返す。不明なら `null`。 */
    fun getWeather(): WeatherType? = weather

    /** 候補のレアリティを登録順に返す。返すリストは変更できない。 */
    fun getRarities(): List<RarityData> = rarities

    /** 候補の魚を全て、レアリティ順に返す。返すリストは変更できない。 */
    fun getFishes(): List<FishData> = Collections.unmodifiableList(rarities.flatMap { fishesByRarity.getValue(it.id) })

    /**
     * 指定したレアリティの候補の魚を返す。候補外のレアリティなら空のリスト。返すリストは変更できない。
     *
     * @param rarity レアリティの ID
     */
    fun getFishes(rarity: RarityId): List<FishData> = fishesByRarity[rarity].orEmpty()

    /** 候補が 1 匹もいなければ `true`。候補が重み 0 の魚だけの場合は `false` だが、抽選結果は `null` になる。 */
    fun isEmpty(): Boolean = fishById.isEmpty()

    /**
     * レアリティの現在の重みを返す。候補外のレアリティなら 0。
     *
     * @param rarity レアリティの ID
     */
    fun getRarityWeight(rarity: RarityId): Double = rarityWeights[rarity] ?: 0.0

    /**
     * レアリティの重みを設定する。
     *
     * @param rarity レアリティの ID
     * @param weight 新しい重み。有限かつ 0 以上
     * @return 設定した場合 `true`、候補外のレアリティなら `false`
     * @throws IllegalArgumentException [weight] が不正な場合
     */
    fun setRarityWeight(
        rarity: RarityId,
        weight: Double,
    ): Boolean {
        requireValid(weight, "weight")
        if (rarity !in rarityWeights) return false
        rarityWeights[rarity] = weight
        return true
    }

    /**
     * レアリティの重みに倍率を掛ける。同じレアリティの魚の確率がまとめて変わり、他のレアリティの確率は相対的に下がる。
     *
     * @param rarity レアリティの ID
     * @param factor 倍率。有限かつ 0 以上
     * @return 変更した場合 `true`、候補外のレアリティなら `false`
     * @throws IllegalArgumentException [factor] が不正な場合
     */
    fun multiplyRarityWeight(
        rarity: RarityId,
        factor: Double,
    ): Boolean {
        requireValid(factor, "factor")
        val current = rarityWeights[rarity] ?: return false
        rarityWeights[rarity] = clamp(current * factor)
        return true
    }

    /**
     * 魚の現在の重みを返す。候補外の魚なら 0。
     *
     * @param fish 魚の ID
     */
    fun getFishWeight(fish: FishId): Double = fishWeights[fish] ?: 0.0

    /**
     * 魚の重み (同じレアリティ内での重み) を設定する。
     *
     * レアリティは重みが正の魚を 1 匹以上持つ間だけ抽選対象になる。
     * 重みが正の魚がいないレアリティの魚に重みを与えるとレアリティが抽選対象に加わり、
     * 最後の重みが正の魚を 0 にするとレアリティが抽選対象から外れる。どちらも他の全てのレアリティの確率を変える。
     *
     * @param fish 魚の ID
     * @param weight 新しい重み。有限かつ 0 以上
     * @return 設定した場合 `true`、候補外の魚なら `false`
     * @throws IllegalArgumentException [weight] が不正な場合
     */
    fun setFishWeight(
        fish: FishId,
        weight: Double,
    ): Boolean {
        requireValid(weight, "weight")
        if (fish !in fishWeights) return false
        fishWeights[fish] = weight
        return true
    }

    /**
     * 魚の重みに倍率を掛ける。通常は同じレアリティ内での割合だけが変わり、全体の確率は倍率どおりにはならない。
     * 全体の確率を倍率どおりに変えたい場合は [multiplyFishProbability] を使う。
     *
     * ただし、レアリティは重みが正の魚を 1 匹以上持つ間だけ抽選対象になる。
     * 魚がレアリティ内で唯一の重みが正の魚で [factor] が 0 の場合は、レアリティが抽選対象から外れ、他の全てのレアリティの確率が上がる。
     *
     * @param fish 魚の ID
     * @param factor 倍率。有限かつ 0 以上
     * @return 変更した場合 `true`、候補外の魚なら `false`
     * @throws IllegalArgumentException [factor] が不正な場合
     */
    fun multiplyFishWeight(
        fish: FishId,
        factor: Double,
    ): Boolean {
        requireValid(factor, "factor")
        val current = fishWeights[fish] ?: return false
        fishWeights[fish] = clamp(current * factor)
        return true
    }

    /**
     * 魚が釣れる全体の確率 ([getProbability]) を [factor] 倍にする。他の全ての候補の確率は互いの比を保ったまま一律に増減する。
     *
     * 現在の確率を `P`、魚のレアリティが選ばれる確率を `p`、目標の確率を `P' = factor * P` とし、
     * 他の候補の確率に掛かる係数を `c = (1 - P') / (1 - P)` とすると、次の 2 つの重みだけを変更する。
     *
     * - 魚の重み: `(P' / P) / c` 倍
     * - レアリティの重み: `(1 / c - 1 + p) / p` 倍
     *
     * 同じレアリティの他の魚の重みと、他のレアリティの重みは変わらない。
     * 魚がレアリティ内で唯一の重みが正の魚の場合は、レアリティの重みだけを変更する。
     *
     * 次の場合は特別に扱う。
     *
     * - `P` が 0 (重み 0 の魚や、重み 0 のレアリティの魚) の場合は何も変更しない。重みを与えるには [setFishWeight] を使う
     * - [factor] が 0 の場合は魚の重みを 0 にする。同じレアリティに重みが正の他の魚がいればレアリティの重みを `1 - q` 倍
     *   (`q` はレアリティ内での魚の割合) にし、他の候補の比を保つ。唯一の魚ならレアリティの重みは変更しない
     * - `P` が 1 (他に抽選対象が無い) で [factor] が正の場合は、確率を移す先が無いため何も変更しない
     * - `P'` が 1 以上になる場合は確率を 1 に丸め、同じレアリティの他の魚と他のレアリティの重みを 0 にする
     *
     * @param fish 魚の ID
     * @param factor 全体の確率に掛ける倍率。有限かつ 0 以上
     * @return 処理した場合 `true`、候補外の魚なら `false`
     * @throws IllegalArgumentException [factor] が不正な場合
     */
    fun multiplyFishProbability(
        fish: FishId,
        factor: Double,
    ): Boolean {
        requireValid(factor, "factor")
        val data = fishById[fish] ?: return false
        val rarity = data.rarity
        val rarityShare = rarityShare(rarity)
        val fishShare = fishShare(fish, rarity)
        val probability = rarityShare * fishShare
        when {
            probability == 0.0 || factor == 1.0 -> Unit
            factor == 0.0 -> zeroProbability(fish, rarity, fishShare)
            probability >= 1.0 -> Unit
            factor * probability >= 1.0 -> saturate(fish, rarity)
            else -> scaleProbability(fish, rarity, factor, probability, rarityShare, fishShare)
        }
        return true
    }

    /**
     * 現在の重みで、この魚が釣れる全体の確率を返す。候補外や抽選対象外の魚なら 0。
     * 全ての魚の確率の合計は、抽選対象があれば 1、無ければ 0 になる。
     *
     * @param fish 魚の ID
     */
    fun getProbability(fish: FishId): Double {
        val data = fishById[fish] ?: return 0.0
        return rarityShare(data.rarity) * fishShare(fish, data.rarity)
    }

    private fun zeroProbability(
        fish: FishId,
        rarity: RarityId,
        fishShare: Double,
    ) {
        val hasOthers = fishesByRarity.getValue(rarity).any { it.id != fish && getFishWeight(it.id) > 0.0 }
        fishWeights[fish] = 0.0
        if (hasOthers) rarityWeights[rarity] = clamp(getRarityWeight(rarity) * (1.0 - fishShare))
    }

    private fun saturate(
        fish: FishId,
        rarity: RarityId,
    ) {
        fishesByRarity.getValue(rarity).filter { it.id != fish }.forEach { fishWeights[it.id] = 0.0 }
        rarityWeights.keys.filter { it != rarity }.forEach { rarityWeights[it] = 0.0 }
    }

    private fun scaleProbability(
        fish: FishId,
        rarity: RarityId,
        factor: Double,
        probability: Double,
        rarityShare: Double,
        fishShare: Double,
    ) {
        val target = factor * probability
        // 他の候補の確率に掛かる係数 c の逆数
        val inverseOthers = (1.0 - probability) / (1.0 - target)
        val rarityFactor = (inverseOthers - 1.0 + rarityShare) / rarityShare
        val newRarityWeight = clamp(getRarityWeight(rarity) * rarityFactor)
        val newFishWeight = if (fishShare < 1.0) clamp(getFishWeight(fish) * factor * inverseOthers) else getFishWeight(fish)
        rarityWeights[rarity] = newRarityWeight
        fishWeights[fish] = newFishWeight
    }

    /** レアリティが選ばれる確率。 */
    private fun rarityShare(rarity: RarityId): Double {
        if (!isDrawable(rarity)) return 0.0
        return share(getRarityWeight(rarity), rarityWeights.keys.filter(::isDrawable).map(::getRarityWeight))
    }

    /** レアリティ内で魚が選ばれる確率。 */
    private fun fishShare(
        fish: FishId,
        rarity: RarityId,
    ): Double = share(getFishWeight(fish), fishesByRarity.getValue(rarity).map { getFishWeight(it.id) })

    private fun isDrawable(rarity: RarityId): Boolean =
        getRarityWeight(rarity) > 0.0 && fishesByRarity[rarity].orEmpty().any { getFishWeight(it.id) > 0.0 }

    private companion object {
        fun requireValid(
            value: Double,
            name: String,
        ) {
            require(value.isFinite() && value >= 0.0) { "$name must be finite and not negative: $value" }
        }

        // 丸め誤差で負にならないよう下限も 0 に揃える
        fun clamp(value: Double): Double = value.coerceIn(0.0, Double.MAX_VALUE)

        /** [values] の合計に対する [value] の割合。合計が溢れないよう最大値で正規化する。 */
        fun share(
            value: Double,
            values: List<Double>,
        ): Double {
            val max = values.maxOrNull() ?: return 0.0
            if (value <= 0.0 || max <= 0.0) return 0.0
            return (value / max) / values.sumOf { it / max }
        }
    }
}
