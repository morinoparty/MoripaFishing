package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.api.utils.serializer.LocaleSerializer
import java.util.Locale

@Serializable
data class FishData(
    // 魚のID ex) tuna
    val id: FishId,
    // 無効にするかどうか(期間限定などを想定) falseであれば普通に釣れる
    val isDisabled: Boolean = false,
    // キャッシュコマンド
    val cacheCommand: List<String> = emptyList(),
    // 表示名
    val displayName: Map<
        @Serializable(
            with = LocaleSerializer::class,
        )
        Locale,
        String,
    > =
        mapOf(
            Locale.JAPAN to "さかな",
        ),
    // 条件
    val conditions: ConditionData,
    // サイズ
    val size: FishSizeData = FishSizeData(20.0, 40.0),
    // 図鑑用 将来のために予約
    val icon: String = "\uE060",
    // アイテムスタック
    val itemStack: ItemStackData,
    // レアリティ
    val rarity: RarityId = RarityId("common"),
    // 重さ
    val weight: Double = 10.0,
    // 価値式 nullの場合 Rarityから取得される
    val worthExpression: String? = null,
    // 説明
    val lore: Map<
        @Serializable(with = LocaleSerializer::class)
        Locale,
        ArrayList<String>,
    > =
        mapOf(
            Locale.JAPAN to arrayListOf(),
            Locale.ENGLISH to arrayListOf(),
        ),
)

@Serializable
data class ConditionData(
    val weather: List<WeatherType> = emptyList(),
    val world: List<FishingWorldId> = emptyList(),
    val time: List<TimeRange> = emptyList(),
)

/**
 * 時間範囲
 * @param start 開始時間(0-95) 0: 00:00, 32: 8:00, 64: 16:00, 95: 23:45
 * @param end 終了時間(0-95) 0: 00:00, 32: 8:00, 64: 16:00, 95: 23:45
 * @example 20:00-04:00 {start: 80, end: 16}
 */
@Serializable
data class TimeRange(
    // 0-95
    val start: Int,
    // 0-95
    val end: Int,
)

@Serializable
data class FishSizeData(
    val min: Double,
    val max: Double,
)

@Serializable
data class ItemStackData(
    val material: String,
    val itemMeta: ItemMetaData,
)

@Serializable
data class ItemMetaData(
    val skullTexture: String? = null,
    val customModelData: List<Float> = listOf(),
)
