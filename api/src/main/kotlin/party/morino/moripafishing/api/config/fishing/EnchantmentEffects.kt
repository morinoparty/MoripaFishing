package party.morino.moripafishing.api.config.fishing

import kotlinx.serialization.Serializable

/**
 * エンチャント効果の設定を保持するデータクラス
 */
@Serializable
data class EnchantmentEffects(
    val lure: Map<Int, LureEffect> = mapOf(
        1 to LureEffect(timeMultiplier = 0.85),
        2 to LureEffect(timeMultiplier = 0.70),
        3 to LureEffect(timeMultiplier = 0.55)
    ),
    val luckOfTheSea: Map<Int, LuckEffect> = mapOf(
        1 to LuckEffect(addSeconds = -0.5),
        2 to LuckEffect(addSeconds = -1.0),
        3 to LuckEffect(addSeconds = -1.5)
    ),
)