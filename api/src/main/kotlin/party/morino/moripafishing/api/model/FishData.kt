package party.morino.moripafishing.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.CompositeEncoder
import party.morino.moripafishing.api.util.LocaleSerializer
import party.morino.moripafishing.api.util.ComponentSerializer
import net.kyori.adventure.text.Component
import party.morino.moripafishing.api.fish.FishId
import party.morino.moripafishing.api.rarity.RarityId
import java.util.Locale

@Serializable
data class FishData(
    val id: FishId, //tuna
    val cacheCommand: List<String> = emptyList(),
    val displayName: Map<@Serializable(with = LocaleSerializer::class) Locale, @Serializable(with = ComponentSerializer::class) Component> = mapOf(
        Locale.JAPAN to Component.text("さかな") //fallback用であり、実際には使用されない予定
    ),
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