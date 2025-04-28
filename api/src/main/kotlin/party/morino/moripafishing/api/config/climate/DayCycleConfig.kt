package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.Serializable

/**
 * 昼夜サイクルの設定を保持するデータクラス
 *
 * @property offset 天気の更新タイミングのオフセット（時間）
 */
@Serializable
data class DayCycleConfig(
    val offset: Int = 0,
) 