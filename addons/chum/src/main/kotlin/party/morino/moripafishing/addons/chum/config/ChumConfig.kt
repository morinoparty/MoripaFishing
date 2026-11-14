package party.morino.moripafishing.addons.chum.config

import kotlinx.serialization.Serializable

/**
 * 撒き餌 Addon の設定 (`config.yml`)。読み込みと検証は [ChumConfigLoader] で行う。
 *
 * @property maxMultiplier 有効な全ての撒き餌の倍率を掛け合わせた後の、レアリティごと・魚ごとの倍率の上限。有限かつ 1 以上
 * @property chums 撒き餌の ID と定義。ID は英小文字・数字・`_`・`-` のみ
 */
@Serializable
data class ChumConfig(
    val maxMultiplier: Double = DEFAULT_MAX_MULTIPLIER,
    val chums: Map<String, ChumDefinition> = emptyMap(),
) {
    companion object {
        /** [maxMultiplier] の既定値 */
        const val DEFAULT_MAX_MULTIPLIER = 100.0
    }
}
