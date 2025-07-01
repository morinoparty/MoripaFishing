package party.morino.moripafishing.api.model.rod

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.core.fishing.ApplyValue

@Serializable
data class RodConfiguration(
    val bonusEffects: Map<@Contextual Regex, List<ApplyValue>> = emptyMap(), // 全てのボーナス効果を統合管理
    val displayNameKey: String = "", // 表示名の翻訳キー
    val loreKeys: List<String> = emptyList(), // Loreの翻訳キー一覧
)
