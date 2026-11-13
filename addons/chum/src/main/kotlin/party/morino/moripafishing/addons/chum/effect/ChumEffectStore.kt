package party.morino.moripafishing.addons.chum.effect

import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.LocationData
import java.time.Clock
import java.time.Duration
import java.util.UUID
import kotlin.math.hypot

/**
 * 有効な撒き餌の効果をメモリ上に保持する。サーバーを再起動すると全て失われる。
 *
 * 期限切れの効果は、参照や変更のたびに取り除く (定期タスクは使わない)。
 * スレッドセーフではない。メインスレッドから使う。
 *
 * @param clock 期限の判定に使う時計
 */
class ChumEffectStore(
    private val clock: Clock,
) {
    private val effects = LinkedHashMap<UUID, ActiveChum>()

    /**
     * 撒き餌の効果を有効にする。
     *
     * 同じ撒き餌の効果が同じ対象に既にあれば、重ねずに期限と回数を更新する。
     * 範囲 ([ChumTarget.Area]) の場合は、同じ撒き餌の範囲の中で使うと、その範囲の期限と回数を更新する (中心は変えない)。
     *
     * @param chumId 撒き餌の ID
     * @param target 効果の対象
     * @param duration 持続時間。期限は今から [duration] 後になる
     * @param charges 効果が続く釣果の回数。`null` なら制限なし
     */
    fun activate(
        chumId: String,
        target: ChumTarget,
        duration: Duration,
        charges: Int?,
    ): ChumActivation {
        removeExpired()
        val expiresAt = clock.instant().plus(duration)
        val existing = effects.values.firstOrNull { it.chumId == chumId && it.target.isRefreshedBy(target) }
        val effect =
            existing?.copy(target = existing.target.refreshedWith(target), expiresAt = expiresAt, remainingCharges = charges)
                ?: ActiveChum(UUID.randomUUID(), chumId, target, expiresAt, charges)
        effects[effect.id] = effect
        return ChumActivation(effect, existing != null)
    }

    /**
     * 抽選に掛かる効果を返す。同じ撒き餌の効果が複数掛かる場合 (範囲が重なる場合) は、最初に有効にした 1 つだけを返す。
     *
     * @param angler 釣りをしたつりびと
     * @param location 釣り針の位置
     */
    fun active(
        angler: AnglerId,
        location: LocationData,
    ): List<ActiveChum> {
        removeExpired()
        return effects.values.filter { it.target.covers(angler, location) }.distinctBy { it.chumId }
    }

    /**
     * 回数の制限がある効果の残り回数を 1 減らす。0 になった効果は取り除く。
     *
     * @param id 効果の ID
     * @return 減らした後の効果。期限切れ・取り除き済み・回数の制限が無い場合は `null`
     */
    fun consumeCharge(id: UUID): ActiveChum? {
        removeExpired()
        val effect = effects[id]
        val charges = effect?.remainingCharges ?: return null
        val remaining = charges - 1
        val updated = effect.copy(remainingCharges = remaining)
        if (remaining > 0) effects[id] = updated else effects.remove(id)
        return updated
    }

    private fun removeExpired() {
        val now = clock.instant()
        effects.values.removeIf { !now.isBefore(it.expiresAt) }
    }

    private fun ChumTarget.isRefreshedBy(other: ChumTarget): Boolean =
        when (this) {
            is ChumTarget.Area -> other is ChumTarget.Area && other.world == world && hypot(other.x - x, other.z - z) <= radius
            else -> this == other
        }

    /** 範囲は中心を保ち、半径だけを新しい定義に合わせる。 */
    private fun ChumTarget.refreshedWith(other: ChumTarget): ChumTarget =
        if (this is ChumTarget.Area && other is ChumTarget.Area) copy(radius = other.radius) else this
}
