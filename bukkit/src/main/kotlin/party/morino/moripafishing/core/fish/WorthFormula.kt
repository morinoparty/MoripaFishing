package party.morino.moripafishing.core.fish

import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder

/**
 * 価値式。変数 `length_rate` (サイズの累積分布関数の値) を使える。
 *
 * exp4j の `Expression` は変数を内部に保持するためスレッドセーフではない。評価ごとに作ること。
 *
 * @param expression 価値式
 * @throws IllegalArgumentException 構文が不正、または未知の変数・関数を使っている場合
 */
internal class WorthFormula(
    expression: String,
) {
    private val compiled: Expression = ExpressionBuilder(expression).variable(LENGTH_RATE).build()

    init {
        val validation = compiled.validate(false)
        require(validation.isValid) { "Invalid worth expression '$expression': ${validation.errors.joinToString()}" }
    }

    /**
     * 価値を計算する。
     *
     * @param lengthRate サイズの累積分布関数の値
     */
    fun evaluate(lengthRate: Double): Double = compiled.setVariable(LENGTH_RATE, lengthRate).evaluate()

    private companion object {
        const val LENGTH_RATE = "length_rate"
    }
}
