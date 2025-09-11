package party.morino.moripafishing.api.model.rarity

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.utils.serializer.LocaleSerializer
import java.util.Locale

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
    val displayName: Map<
        @Serializable(with = LocaleSerializer::class)
        Locale,
        String,
    > =
        mapOf(
            Locale.JAPAN to "レアリティ",
        ),
    val weight: Double = 1.0,
    val worthExpression: String = "1",
)
