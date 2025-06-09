package party.morino.moripafishing.api.model.rod

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.core.fishing.ApplyValue

@Serializable
data class RodConfiguration(
    val rodType: String,
    val waitTimeMultiplier: Double = 1.0,
    val bonusEffects: List<ApplyValue> = emptyList(),
    val weatherImmunity: Boolean = false,
    val biomeBonuses: Map<String, Double> = emptyMap(),
    val displayName: String = "",
    val lore: List<String> = emptyList(),
)