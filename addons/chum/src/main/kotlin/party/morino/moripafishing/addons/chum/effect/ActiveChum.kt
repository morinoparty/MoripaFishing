package party.morino.moripafishing.addons.chum.effect

import java.time.Instant
import java.util.UUID

/**
 * 有効な撒き餌の効果。
 *
 * 撒き餌の定義は ID で持ち、抽選のたびに現在の設定から引く (再読み込みで定義が変わっても追従する)。
 *
 * @property id 効果の ID
 * @property chumId 撒き餌の ID
 * @property target 効果の対象
 * @property expiresAt 効果が切れる時刻
 * @property remainingCharges 残りの釣果の回数。`null` なら回数の制限なし
 */
data class ActiveChum(
    val id: UUID,
    val chumId: String,
    val target: ChumTarget,
    val expiresAt: Instant,
    val remainingCharges: Int?,
)
