package party.morino.moripafishing.core.fishing

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.fishing.WaitTimeManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Spot
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.max
import kotlin.math.min

/**
 * WaitTimeManagerの実装クラス
 *
 * 現状は単純なメモリ管理。今後永続化や詳細なロジックを追加する場合はここを拡張する。
 */

class WaitTimeManagerImpl : WaitTimeManager, KoinComponent {
    private val configManager: ConfigManager by inject()

    // 各種適用値を保持するマップ
    private val spotValues: MutableList<Triple<Spot, ApplyValue, ZonedDateTime?>> = mutableListOf()
    private val anglerValues: MutableList<Triple<AnglerId, ApplyValue, ZonedDateTime?>> = mutableListOf()
    private val worldValues: MutableList<Triple<FishingWorldId, ApplyValue, ZonedDateTime?>> = mutableListOf()

    /**
     * Spot単位で適用値を設定
     * @param spot スポット
     * @param applyValue 適用値
     * @param limit 有効期限(ミリ秒)
     */
    override fun applyForSpot(
        spot: Spot,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val limitDateTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        spotValues.add(Triple(spot, applyValue, limitDateTime))
    }

    /**
     * Angler単位で適用値を設定
     */
    override fun applyForAngler(
        anglerId: AnglerId,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val limitDateTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        anglerValues.add(Triple(anglerId, applyValue, limitDateTime))
    }

    /**
     * World単位で適用値を設定
     */
    override fun applyForWorld(
        worldId: FishingWorldId,
        applyValue: ApplyValue,
        limit: Long?,
    ) {
        val limitDateTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        worldValues.add(Triple(worldId, applyValue, limitDateTime))
    }

    /**
     * 釣り人に対する待機時間を取得
     * World → Angler → Spot の順で適用値を合成して最終的な待機時間を計算
     */
    override fun getWaitTime(angler: Angler): Pair<Int, Int> {
        // 設定からベース待機時間を取得
        val fishingConfig = configManager.getConfig().fishing
        var baseMinTime = fishingConfig.baseWaitTime.minSeconds
        var baseMaxTime = fishingConfig.baseWaitTime.maxSeconds

        // 期限切れの値を削除
        cleanExpiredValues()

        // World レベルの適用値を取得・適用
        angler.getWorld()?.let { world ->
            val worldEffects = getWorldEffects(world.getId())
            for (effect in worldEffects) {
                val result = applyEffect(baseMinTime, baseMaxTime, effect)
                baseMinTime = result.first
                baseMaxTime = result.second
            }
        }

        // Angler レベルの適用値を取得・適用
        val anglerEffects = getAnglerEffects(angler.getAnglerUniqueId())
        for (effect in anglerEffects) {
            val result = applyEffect(baseMinTime, baseMaxTime, effect)
            baseMinTime = result.first
            baseMaxTime = result.second
        }

        // Spot レベルの適用値を取得・適用 (位置からスポットを計算)
        angler.getLocation()?.let { location ->
            val spot = party.morino.moripafishing.api.model.world.Spot(location, 1.0)
            val spotEffects = getSpotEffects(spot)
            for (effect in spotEffects) {
                val result = applyEffect(baseMinTime, baseMaxTime, effect)
                baseMinTime = result.first
                baseMaxTime = result.second
            }
        }

        // 設定からの絶対制限値を適用
        val finalMinTime = max(fishingConfig.baseWaitTime.absoluteMinSeconds, baseMinTime).toInt()
        val finalMaxTime = max(finalMinTime.toDouble(), min(fishingConfig.baseWaitTime.absoluteMaxSeconds, baseMaxTime)).toInt()

        return finalMinTime to finalMaxTime
    }

    private fun cleanExpiredValues() {
        val now = ZonedDateTime.now()
        spotValues.removeAll { it.third?.isBefore(now) == true }
        anglerValues.removeAll { it.third?.isBefore(now) == true }
        worldValues.removeAll { it.third?.isBefore(now) == true }
    }

    private fun getWorldEffects(worldId: FishingWorldId): List<ApplyValue> {
        return worldValues
            .filter { it.first == worldId }
            .filter { it.third?.isAfter(ZonedDateTime.now()) != false }
            .map { it.second }
    }

    private fun getAnglerEffects(anglerId: AnglerId): List<ApplyValue> {
        return anglerValues
            .filter { it.first == anglerId }
            .filter { it.third?.isAfter(ZonedDateTime.now()) != false }
            .map { it.second }
    }

    private fun getSpotEffects(spot: Spot): List<ApplyValue> {
        return spotValues
            .filter { it.first == spot }
            .filter { it.third?.isAfter(ZonedDateTime.now()) != false }
            .map { it.second }
    }

    private fun applyEffect(
        minTime: Double,
        maxTime: Double,
        effect: ApplyValue,
    ): Pair<Double, Double> {
        return when (effect.type) {
            ApplyType.ADD -> {
                Pair(minTime + effect.value, maxTime + effect.value)
            }
            ApplyType.MULTIPLY -> {
                Pair(minTime * effect.value, maxTime * effect.value)
            }
            ApplyType.CONSTANT -> {
                // CONSTANTの場合は値を固定時間として使用
                Pair(effect.value, effect.value)
            }
        }
    }

    /**
     * 特定のアングラーの適用値をクリア（釣り終了時に使用）
     */
    fun clearAnglerEffects(anglerId: AnglerId) {
        anglerValues.removeAll { it.first == anglerId }
    }
}
