package party.morino.moripafishing.api.config.fishing

import kotlinx.serialization.Serializable

/**
 * 基本待ち時間の設定を保持するデータクラス
 */
@Serializable
data class BaseWaitTimeConfig(
    val minSeconds: Double = 5.0,
    val maxSeconds: Double = 30.0,
    val absoluteMinSeconds: Double = 0.5,
    val absoluteMaxSeconds: Double = 300.0,
)