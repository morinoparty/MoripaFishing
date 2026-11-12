package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.serialization.LocaleSerializer
import java.util.Locale

/**
 * 魚の定義。`fish/` 配下の JSON ファイル (サブディレクトリを含む) から読み込まれる。
 *
 * @property id 魚の ID
 * @property rarity レアリティの ID。登録済みのレアリティであること
 * @property displayName ロケール別の表示名 (MiniMessage)
 * @property lore ロケール別の説明文 (MiniMessage)。`<rarity>` `<size>` `<angler>` `<world>` `<timestamp>` を使える
 * @property item アイテムの見た目
 * @property size サイズの範囲
 * @property weight 同じレアリティ内での抽選の重み。0 以上
 * @property worth 価値式 (変数 `length_rate`)。`null` ならレアリティの価値式を使う
 * @property conditions 釣れる条件
 * @property enabled `false` なら抽選の対象外
 */
@Serializable
data class FishData(
    val id: FishId,
    val rarity: RarityId,
    val displayName: Map<
        @Serializable(with = LocaleSerializer::class)
        Locale,
        String,
    >,
    val lore: Map<
        @Serializable(with = LocaleSerializer::class)
        Locale,
        List<String>,
    > = emptyMap(),
    val item: FishItemData,
    val size: FishSize,
    val weight: Double = 1.0,
    val worth: String? = null,
    val conditions: FishConditions = FishConditions(),
    val enabled: Boolean = true,
) {
    init {
        require(weight >= 0.0) { "weight must not be negative: $weight" }
    }
}
