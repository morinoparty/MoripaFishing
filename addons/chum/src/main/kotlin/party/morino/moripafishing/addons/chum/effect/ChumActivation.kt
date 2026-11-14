package party.morino.moripafishing.addons.chum.effect

/**
 * 撒き餌を使った結果。
 *
 * @property effect 有効になった効果
 * @property refreshed 同じ撒き餌の既存の効果を更新した場合 `true`、新しい効果の場合 `false`
 */
data class ChumActivation(
    val effect: ActiveChum,
    val refreshed: Boolean,
)
