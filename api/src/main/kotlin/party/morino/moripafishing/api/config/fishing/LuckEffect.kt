package party.morino.moripafishing.api.config.fishing

import kotlinx.serialization.Serializable

/**
 * 海運エンチャントの効果設定を保持するデータクラス
 */
@Serializable
data class LuckEffect(
    val addSeconds: Double,
)