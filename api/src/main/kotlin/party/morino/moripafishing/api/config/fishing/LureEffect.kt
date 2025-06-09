package party.morino.moripafishing.api.config.fishing

import kotlinx.serialization.Serializable

/**
 * 入れ食いエンチャントの効果設定を保持するデータクラス
 */
@Serializable
data class LureEffect(
    val timeMultiplier: Double,
)