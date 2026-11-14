package party.morino.moripafishing.addons.chum.config

import kotlinx.serialization.Serializable

/**
 * 撒き餌 1 種類の定義。ID は [ChumConfig.chums] のキー。
 *
 * @property item アイテムの見た目
 * @property scope 効果の範囲
 * @property durationSeconds 効果の持続時間 (秒)
 * @property charges [ChumScope.ANGLER] のときだけ指定できる、効果が続く釣果の回数。`null` なら時間だけで切れる
 * @property radius [ChumScope.AREA] のときに必須の、使用位置からの水平距離 (ブロック)
 * @property rarityMultipliers レアリティ ID ごとの重みの倍率
 * @property fishMultipliers 魚 ID ごとの、魚が釣れる全体の確率の倍率
 * @property fishWeights 魚 ID ごとに設定する重み。重み 0 の魚 (餌限定の魚) を釣れるようにする
 */
@Serializable
data class ChumDefinition(
    val item: ChumItemData,
    val scope: ChumScope,
    val durationSeconds: Long,
    val charges: Int? = null,
    val radius: Double? = null,
    val rarityMultipliers: Map<String, Double> = emptyMap(),
    val fishMultipliers: Map<String, Double> = emptyMap(),
    val fishWeights: Map<String, Double> = emptyMap(),
)
