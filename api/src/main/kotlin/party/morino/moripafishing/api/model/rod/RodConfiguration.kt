package party.morino.moripafishing.api.model.rod

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.core.fishing.ApplyValue

@Serializable
data class RodConfiguration(
    val rodType: RodPresetId,
    val waitTimeMultiplier: Double = 1.0,
    val bonusEffects: List<ApplyValue> = emptyList(),
    val weatherImmunity: Boolean = false,
    val fishingWorldBonuses: Map<String, Double> = emptyMap(),
    val displayNameKey: String = "",
    val loreKeys: List<String> = emptyList(),
)
