package party.morino.moripafishing.core.random.fish

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.random.fish.FishProbabilityManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.api.model.world.Spot
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 魚やレアリティの確率を動的に管理する実装クラス
 * WaitTimeManagerの仕組みを参考に、確率修正値を管理する
 * 初期化時に全ての魚とレアリティの基本重みを読み込む
 */
class FishProbabilityManagerImpl : FishProbabilityManager, KoinComponent {
    // 基本重みを保持するマップ
    private val baseRarityWeights: MutableMap<RarityId, Double> = mutableMapOf()
    private val baseFishWeights: MutableMap<FishId, Double> = mutableMapOf()

    // 魚とレアリティマネージャーを注入
    private val fishManager: FishManager by inject()
    private val rarityManager: RarityManager by inject()

    init {
        // 初期化時に全ての基本重みを読み込み
        loadAllBaseWeights()
    }

    /**
     * 全ての魚とレアリティの基本重みを読み込む
     */
    private fun loadAllBaseWeights() {
        // レアリティの基本重みを読み込み
        rarityManager.getRarities().forEach { rarity ->
            baseRarityWeights[rarity.id] = rarity.weight
        }

        // 魚の基本重みを読み込み
        fishManager.getFish().forEach { fish ->
            baseFishWeights[fish.id] = fish.weight
        }
    }

    // レアリティ修正値を保持するマップ
    private val spotRarityModifiers: MutableList<RarityModifier> = mutableListOf()
    private val anglerRarityModifiers: MutableList<RarityModifier> = mutableListOf()
    private val worldRarityModifiers: MutableList<RarityModifier> = mutableListOf()
    private val rodRarityModifiers: MutableList<RarityModifier> = mutableListOf()

    // 魚修正値を保持するマップ
    private val spotFishModifiers: MutableList<FishModifier> = mutableListOf()
    private val anglerFishModifiers: MutableList<FishModifier> = mutableListOf()
    private val worldFishModifiers: MutableList<FishModifier> = mutableListOf()
    private val rodFishModifiers: MutableList<FishModifier> = mutableListOf()

    /**
     * レアリティ修正値のデータクラス
     */
    private data class RarityModifier(
        val target: Any, // Spot, AnglerId, FishingWorldId
        val rarityId: RarityId,
        val applyValue: ApplyValue,
        val expirationTime: ZonedDateTime?,
    )

    /**
     * 魚修正値のデータクラス
     */
    private data class FishModifier(
        val target: Any, // Spot, AnglerId, FishingWorldId
        val fishId: FishId,
        val applyValue: ApplyValue,
        val expirationTime: ZonedDateTime?,
    )

    override fun applyRarityModifierForSpot(
        spot: Spot,
        rarityId: RarityId,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val expirationTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        spotRarityModifiers.add(RarityModifier(spot, rarityId, applyValue, expirationTime))
    }

    override fun applyRarityModifierForAngler(
        anglerId: AnglerId,
        rarityId: RarityId,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val expirationTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        anglerRarityModifiers.add(RarityModifier(anglerId, rarityId, applyValue, expirationTime))
    }

    override fun applyRarityModifierForWorld(
        worldId: FishingWorldId,
        rarityId: RarityId,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val expirationTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        worldRarityModifiers.add(RarityModifier(worldId, rarityId, applyValue, expirationTime))
    }

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

    override fun applyRarityModifierForRod(
        rodType: String,
        rarityId: RarityId,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val expirationTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        rodRarityModifiers.add(RarityModifier(rodType, rarityId, applyValue, expirationTime))
    }

    override fun applyFishModifierForRod(
        rodType: String,
        fishId: FishId,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val expirationTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        rodFishModifiers.add(FishModifier(rodType, fishId, applyValue, expirationTime))
    }

    override fun setBaseRarityWeight(
        rarityId: RarityId,
        baseWeight: Double,
    ) {
        baseRarityWeights[rarityId] = baseWeight
    }

    override fun setBaseFishWeight(
        fishId: FishId,
        baseWeight: Double,
    ) {
        baseFishWeights[fishId] = baseWeight
    }

    override fun getBaseRarityWeight(rarityId: RarityId): Double {
        return baseRarityWeights[rarityId] ?: 1.0
    }

    override fun getBaseFishWeight(fishId: FishId): Double {
        return baseFishWeights[fishId] ?: 1.0
    }

    override fun getModifiedRarityWeight(
        angler: Angler,
        rarityId: RarityId,
    ): Double {
        cleanupExpiredModifiers()

        var modifiedWeight = getBaseRarityWeight(rarityId)

        // World レベルの修正値を適用
        angler.getWorld()?.let { world ->
            val worldEffects = getWorldRarityEffects(world.getId(), rarityId)
            for (effect in worldEffects) {
                modifiedWeight = applyWeightEffect(modifiedWeight, effect)
            }
        }

        // 釣り人レベルの修正値を適用
        val anglerEffects = getAnglerRarityEffects(angler.getAnglerUniqueId(), rarityId)
        for (effect in anglerEffects) {
            modifiedWeight = applyWeightEffect(modifiedWeight, effect)
        }

        // Spot レベルの修正値を適用（釣り針の位置を使用）
        angler.getFishingHookLocation()?.let { location ->
            val spotEffects = getSpotRarityEffectsForLocation(location, rarityId)
            for (effect in spotEffects) {
                modifiedWeight = applyWeightEffect(modifiedWeight, effect)
            }
        }

        // Rod レベルの修正値を適用
        angler.getCurrentRodConfiguration()?.let { rodConfig ->
            val rodEffects = getRodRarityEffects(rodConfig.rodType, rarityId)
            for (effect in rodEffects) {
                modifiedWeight = applyWeightEffect(modifiedWeight, effect)
            }
        }

        // 重みは0以上にする
        return max(0.0, modifiedWeight)
    }

    override fun getModifiedFishWeight(
        angler: Angler,
        fishId: FishId,
    ): Double {
        cleanupExpiredModifiers()

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
        angler.getFishingHookLocation()?.let { location ->
            val spotEffects = getSpotFishEffectsForLocation(location, fishId)
            for (effect in spotEffects) {
                modifiedWeight = applyWeightEffect(modifiedWeight, effect)
            }
        }

        // Rod レベルの修正値を適用
        angler.getCurrentRodConfiguration()?.let { rodConfig ->
            val rodEffects = getRodFishEffects(rodConfig.rodType, fishId)
            for (effect in rodEffects) {
                modifiedWeight = applyWeightEffect(modifiedWeight, effect)
            }
        }

        // 重みは0以上にする
        return max(0.0, modifiedWeight)
    }

    override fun clearAnglerModifiers(anglerId: AnglerId) {
        anglerRarityModifiers.removeAll { it.target == anglerId }
        anglerFishModifiers.removeAll { it.target == anglerId }
    }

    override fun cleanupExpiredModifiers() {
        val now = ZonedDateTime.now()

        // レアリティ修正値のクリーンアップ
        spotRarityModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
        anglerRarityModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
        worldRarityModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
        rodRarityModifiers.removeAll { it.expirationTime?.isBefore(now) == true }

        // 魚修正値のクリーンアップ
        spotFishModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
        anglerFishModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
        worldFishModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
        rodFishModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
    }

    // プライベートヘルパーメソッド

    private fun getWorldRarityEffects(
        worldId: FishingWorldId,
        rarityId: RarityId,
    ): List<ApplyValue> {
        return worldRarityModifiers
            .filter { it.target == worldId && it.rarityId == rarityId }
            .filter { it.expirationTime?.isAfter(ZonedDateTime.now()) != false }
            .map { it.applyValue }
    }

    private fun getAnglerRarityEffects(
        anglerId: AnglerId,
        rarityId: RarityId,
    ): List<ApplyValue> {
        return anglerRarityModifiers
            .filter { it.target == anglerId && it.rarityId == rarityId }
            .filter { it.expirationTime?.isAfter(ZonedDateTime.now()) != false }
            .map { it.applyValue }
    }

    private fun getSpotRarityEffectsForLocation(
        location: Location,
        rarityId: RarityId,
    ): List<ApplyValue> {
        return spotRarityModifiers
            .filter { modifier ->
                val spot = modifier.target as? Spot ?: return@filter false
                modifier.rarityId == rarityId &&
                    modifier.expirationTime?.isAfter(ZonedDateTime.now()) != false &&
                    isLocationInSpot(location, spot)
            }
            .map { it.applyValue }
    }

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

    private fun getRodRarityEffects(
        rodType: String,
        rarityId: RarityId,
    ): List<ApplyValue> {
        return rodRarityModifiers
            .filter { it.target == rodType && it.rarityId == rarityId }
            .filter { it.expirationTime?.isAfter(ZonedDateTime.now()) != false }
            .map { it.applyValue }
    }

    private fun getRodFishEffects(
        rodType: String,
        fishId: FishId,
    ): List<ApplyValue> {
        return rodFishModifiers
            .filter { it.target == rodType && it.fishId == fishId }
            .filter { it.expirationTime?.isAfter(ZonedDateTime.now()) != false }
            .map { it.applyValue }
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
