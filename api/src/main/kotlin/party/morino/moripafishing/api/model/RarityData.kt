package party.morino.moripafishing.api.model

import kotlinx.serialization.Serializable
import java.util.Locale
import party.morino.moripafishing.api.util.LocaleSerializer
import party.morino.moripafishing.api.util.ComponentSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import party.morino.moripafishing.api.rarity.RarityId

/**
 * 魚のレアリティを表すクラス
 * @param id レアリティのID
 * @param color レアリティの色 Hex
 * @param displayName レアリティの表示名
 * @param weight レアリティの重み
 */
@Serializable
data class RarityData(
    val id: RarityId = RarityId("common"),
    val color: String = "#ffffff",
    val cacheCommand: List<String> = emptyList(),
    val displayName: Map<@Serializable(with = LocaleSerializer::class) Locale, @Serializable(with = ComponentSerializer::class) Component> = mapOf(
        Locale.JAPAN to Component.text("レアリティ")
    ),
    val weight: Double = 1.0,
    val worthExpression: String = "1"
){
    override fun toString(): String {
        val displayName = this.displayName.map { "${it.key.toLanguageTag()}: ${MiniMessage.miniMessage().serialize(it.value)}"}.joinToString(", ")
        return "id : ${this.id.value}, displayName: [${displayName}], weight : ${this.weight}"
    }
}