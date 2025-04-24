package party.morino.moripafishing.api.config.fish

import kotlinx.serialization.Serializable

/**
 * 釣りの設定を保持するデータクラス
 */
@Serializable
data class FishingConfig(
    val test: String = "test",
)
