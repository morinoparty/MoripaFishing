package party.morino.moripafishing.core.random.fish

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.random.fish.FishProbabilityManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rod.getEffectsForWorld
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.api.model.world.Spot
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 魚の確率を動的に管理する実装クラス
 * WaitTimeManagerの仕組みを参考に、確率修正値を管理する
 * 初期化時に全ての魚の基本重みを読み込む
 */
class FishProbabilityManagerImpl : FishProbabilityManager, KoinComponent {
    // 基本重みを保持するマップ
    private val baseFishWeights: MutableMap<FishId, Double> = mutableMapOf()

    // 魚マネージャーを注入
    private val fishManager: FishManager by inject()

    init {
        // 初期化時に全ての基本重みを読み込み
        loadAllBaseWeights()
    }

    /**
     * 全ての魚の基本重みを読み込む
     */
    private fun loadAllBaseWeights() {
        // 魚の基本重みを読み込み
        fishManager.getFish().forEach { fish ->
            baseFishWeights[fish.id] = fish.weight
        }
    }

    // 魚修正値を保持するマップ
    private val spotFishModifiers: MutableList<FishModifier> = mutableListOf()
    private val anglerFishModifiers: MutableList<FishModifier> = mutableListOf()
    private val worldFishModifiers: MutableList<FishModifier> = mutableListOf()

    /**
     * 魚修正値のデータクラス
     */
    private data class FishModifier(
        val target: Any, // Spot, AnglerId, FishingWorldId
        val fishId: FishId,
        val applyValue: ApplyValue,
        val expirationTime: ZonedDateTime?,
    )

    override fun applyFishModifierForSpot(
        spot: Spot,
        fishId: FishId,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val expirationTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        spotFishModifiers.add(FishModifier(spot, fishId, applyValue, expirationTime))
    }

    override fun applyFishModifierForAngler(
        anglerId: AnglerId,
        fishId: FishId,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val expirationTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        anglerFishModifiers.add(FishModifier(anglerId, fishId, applyValue, expirationTime))
    }

    override fun applyFishModifierForWorld(
        worldId: FishingWorldId,
        fishId: FishId,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val expirationTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        worldFishModifiers.add(FishModifier(worldId, fishId, applyValue, expirationTime))
    }

    override fun setBaseFishWeight(
        fishId: FishId,
        baseWeight: Double,
    ) {
        baseFishWeights[fishId] = baseWeight
    }

    override fun getBaseFishWeight(fishId: FishId): Double {
        return baseFishWeights[fishId] ?: 1.0
    }

    override fun getModifiedFishWeight(
        angler: Angler,
        fishId: FishId,
    ): Double {
        cleanupExpiredFishModifiers()

        var modifiedWeight = getBaseFishWeight(fishId)

        // World レベルの修正値を適用
        angler.getWorld()?.let { world ->
            val worldEffects = getWorldFishEffects(world.getId(), fishId)
            for (effect in worldEffects) {
                modifiedWeight = applyWeightEffect(modifiedWeight, effect)
            }
        }

        // 釣り人レベルの修正値を適用
        val anglerEffects = getAnglerFishEffects(angler.getAnglerUniqueId(), fishId)
        for (effect in anglerEffects) {
            modifiedWeight = applyWeightEffect(modifiedWeight, effect)
        }

        // Spot レベルの修正値を適用（釣り針の位置を使用）
        angler.getCurrentRod()?.getHookLocation()?.let { location ->
            val spotEffects = getSpotFishEffectsForLocation(location, fishId)
            for (effect in spotEffects) {
                modifiedWeight = applyWeightEffect(modifiedWeight, effect)
            }
        }

        // Rod レベルの修正値を適用（bonusEffectsから直接取得）
        angler.getCurrentRod()?.configuration?.let { rodConfig ->
            // 現在のワールド名を取得
            val worldName = angler.getWorld()?.getId()?.value

            // ワールドに適合するロッド効果を取得
            val rodEffects = rodConfig.getEffectsForWorld(worldName)

            // 魚関連の効果をフィルタリングして適用
            val fishEffects =
                rodEffects.filter { effect ->
                    // 魚IDでフィルタリングするロジックを実装
                    // 例: effect.target?.contains(fish.id.value) == true
                    // 現在は全ての効果を適用
                    true
                }

            fishEffects.forEach { effect ->
                modifiedWeight = applyWeightEffect(modifiedWeight, effect)
            }
        }

        // 重みは0以上にする
        return max(0.0, modifiedWeight)
    }

    override fun clearAnglerFishModifiers(anglerId: AnglerId) {
        anglerFishModifiers.removeAll { it.target == anglerId }
    }

    override fun cleanupExpiredFishModifiers() {
        val now = ZonedDateTime.now()

        // 魚修正値のクリーンアップ
        spotFishModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
        anglerFishModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
        worldFishModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
    }

    override fun cleanupAllFishModifiers() {
        spotFishModifiers.clear()
        anglerFishModifiers.clear()
        worldFishModifiers.clear()
    }

    // プライベートヘルパーメソッド

    private fun getWorldFishEffects(
        worldId: FishingWorldId,
        fishId: FishId,
    ): List<ApplyValue> {
        return worldFishModifiers
            .filter { it.target == worldId && it.fishId == fishId }
            .filter { it.expirationTime?.isAfter(ZonedDateTime.now()) != false }
            .map { it.applyValue }
    }

    private fun getAnglerFishEffects(
        anglerId: AnglerId,
        fishId: FishId,
    ): List<ApplyValue> {
        return anglerFishModifiers
            .filter { it.target == anglerId && it.fishId == fishId }
            .filter { it.expirationTime?.isAfter(ZonedDateTime.now()) != false }
            .map { it.applyValue }
    }

    private fun getSpotFishEffectsForLocation(
        location: Location,
        fishId: FishId,
    ): List<ApplyValue> {
        return spotFishModifiers
            .filter { modifier ->
                val spot = modifier.target as? Spot ?: return@filter false
                modifier.fishId == fishId &&
                    modifier.expirationTime?.isAfter(ZonedDateTime.now()) != false &&
                    isLocationInSpot(location, spot)
            }
            .map { it.applyValue }
    }

    private fun isLocationInSpot(
        location: Location,
        spot: Spot,
    ): Boolean {
        // 同じワールドかチェック
        if (spot.location.worldId != location.worldId) return false

        // 距離チェック（プレイヤーがSpotの範囲内にいるか）
        val distance =
            sqrt(
                (spot.location.x - location.x).pow(2.0) +
                    (spot.location.z - location.z).pow(2.0),
            )
        return distance <= spot.radius
    }

    private fun applyWeightEffect(
        weight: Double,
        effect: ApplyValue,
    ): Double {
        return when (effect.type) {
            ApplyType.ADD -> weight + effect.value
            ApplyType.MULTIPLY -> weight * effect.value
            ApplyType.CONSTANT -> effect.value
        }
    }
}
