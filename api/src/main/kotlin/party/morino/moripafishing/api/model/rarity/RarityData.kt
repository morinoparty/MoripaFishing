package party.morino.moripafishing.api.model.rarity

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.serialization.LocaleSerializer
import java.util.Locale

/**
 * レアリティの定義。`rarity/` 配下の JSON ファイル (サブディレクトリを含む) から読み込まれる。
 *
 * @property id レアリティの ID
 * @property displayName ロケール別の表示名 (MiniMessage)
 * @property weight レアリティの抽選の重み。0 以上
 * @property worth 価値式 (変数 `length_rate`)。魚が価値式を持たない場合に使う
 */
@Serializable
data class RarityData(
    val id: RarityId,
    val displayName: Map<
        @Serializable(with = LocaleSerializer::class)
        Locale,
        String,
    >,
    val weight: Double,
    val worth: String,
) {
    init {
        require(weight >= 0.0) { "weight must not be negative: $weight" }
    }
}
