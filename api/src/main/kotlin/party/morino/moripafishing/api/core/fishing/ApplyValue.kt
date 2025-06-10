package party.morino.moripafishing.api.core.fishing

import kotlinx.serialization.Serializable

@Serializable
data class ApplyValue(
    val type: ApplyType,
    val value: Double,
    val unit: String,
)
