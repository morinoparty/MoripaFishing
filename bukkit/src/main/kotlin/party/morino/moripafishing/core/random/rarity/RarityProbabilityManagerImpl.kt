package party.morino.moripafishing.core.random.rarity

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.random.rarity.RarityProbabilityManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.rarity.RarityId
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
 * レアリティの確率を動的に管理する実装クラス
 * WaitTimeManagerの仕組みを参考に、確率修正値を管理する
 * 初期化時に全てのレアリティの基本重みを読み込む
 */
class RarityProbabilityManagerImpl : RarityProbabilityManager, KoinComponent {
    // 基本重みを保持するマップ
    private val baseRarityWeights: MutableMap<RarityId, Double> = mutableMapOf()

    // レアリティマネージャーを注入
    private val rarityManager: RarityManager by inject()

    init {
        // 初期化時に全ての基本重みを読み込み
        loadAllBaseWeights()
    }

    /**
     * 全てのレアリティの基本重みを読み込む
     */
    private fun loadAllBaseWeights() {
        // レアリティの基本重みを読み込み
        rarityManager.getRarities().forEach { rarity ->
            baseRarityWeights[rarity.id] = rarity.weight
        }
    }

    // レアリティ修正値を保持するマップ
    private val spotRarityModifiers: MutableList<RarityModifier> = mutableListOf()
    private val anglerRarityModifiers: MutableList<RarityModifier> = mutableListOf()
    private val worldRarityModifiers: MutableList<RarityModifier> = mutableListOf()

    /**
     * レアリティ修正値のデータクラス
     */
    private data class RarityModifier(
        val target: Any, // Spot, AnglerId, FishingWorldId
        val rarityId: RarityId,
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

    override fun setBaseRarityWeight(
        rarityId: RarityId,
        baseWeight: Double,
    ) {
        baseRarityWeights[rarityId] = baseWeight
    }

    override fun getBaseRarityWeight(rarityId: RarityId): Double {
        return baseRarityWeights[rarityId] ?: 1.0
    }

    override fun getModifiedRarityWeight(
        angler: Angler,
        rarityId: RarityId,
    ): Double {
        cleanupExpiredRarityModifiers()

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
        angler.getCurrentRod()?.getHookLocation()?.let { location ->
            val spotEffects = getSpotRarityEffectsForLocation(location, rarityId)
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

            // レアリティ関連の効果をフィルタリングして適用
            val rarityEffects =
                rodEffects.filter { effect ->
                    // レアリティIDでフィルタリングするロジックを実装
                    // 例: effect.target?.contains(rarity.id.value) == true
                    // 現在は全ての効果を適用
                    true
                }

            rarityEffects.forEach { effect ->
                modifiedWeight = applyWeightEffect(modifiedWeight, effect)
            }
        }

        // 重みは0以上にする
        return max(0.0, modifiedWeight)
    }

    override fun clearAnglerRarityModifiers(anglerId: AnglerId) {
        anglerRarityModifiers.removeAll { it.target == anglerId }
    }

    override fun cleanupExpiredRarityModifiers() {
        val now = ZonedDateTime.now()

        // レアリティ修正値のクリーンアップ
        spotRarityModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
        anglerRarityModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
        worldRarityModifiers.removeAll { it.expirationTime?.isBefore(now) == true }
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
