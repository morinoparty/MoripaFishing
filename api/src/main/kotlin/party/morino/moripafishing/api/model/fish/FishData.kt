package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.utils.ComponentSerializer
import party.morino.moripafishing.api.utils.LocaleSerializer
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import java.util.*

@Serializable
data class FishData(
    val id: FishId, //tuna
    val cacheCommand: List<String> = emptyList(),
    val displayName: Map<@Serializable(with = LocaleSerializer::class) Locale, @Serializable(with = ComponentSerializer::class) Component> = mapOf(
        Locale.JAPAN to Component.text("さかな") //fallback用であり、実際には使用されない予定
    ),
    val conditions: ConditionData,
    val size : FishSizeData = FishSizeData(20.0, 40.0),
    val icon : String = "\uE060", //図鑑用 将来のために予約
    val itemStack : ItemStackData,
    val rarity : RarityId = RarityId("common"),
    val weight: Double = 10.0,
    val worthExpression : String? = null, //nullの場合 Rarityから取得される
    val lore : Map<@Serializable(with = LocaleSerializer::class) Locale, ArrayList< @Serializable(with = ComponentSerializer::class) Component>> = mapOf(
        Locale.JAPAN to arrayListOf(),
        Locale.ENGLISH to arrayListOf()
    )
)

@Serializable
data class ConditionData(
    val weather: List<WeatherType> = emptyList(),
    val world: List<FishingWorldId> = emptyList(),
)

@Serializable
data class FishSizeData(
    val min: Double,
    val max: Double,
)

@Serializable
data class ItemStackData(
    val material : String,
    val itemMeta : ItemMetaData,
)

@Serializable
data class ItemMetaData(
    val skullTexture : String? = null,
    val customModelData : Int? = null
)