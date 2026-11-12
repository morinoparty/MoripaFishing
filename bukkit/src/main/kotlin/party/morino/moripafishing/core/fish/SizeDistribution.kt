package party.morino.moripafishing.core.fish

import party.morino.moripafishing.api.model.fish.FishSize
import java.util.random.RandomGenerator
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * 魚のサイズの分布。平均 `(min + max) / 2`・標準偏差 `(max - min) / 6` の正規分布を範囲内に収めたもの。
 */
internal object SizeDistribution {
    /** 範囲の幅を標準偏差の何倍とみなすか。 */
    private const val RANGE_IN_SD = 6.0

    // Abramowitz and Stegun 7.1.26 による誤差関数の近似係数
    private const val ERF_P = 0.3275911
    private const val ERF_A1 = 0.254829592
    private const val ERF_A2 = -0.284496736
    private const val ERF_A3 = 1.421413741
    private const val ERF_A4 = -1.453152027
    private const val ERF_A5 = 1.061405429

    /**
     * サイズを抽選する。
     *
     * @param size サイズの範囲
     * @param random 乱数生成器
     */
    fun sample(
        size: FishSize,
        random: RandomGenerator,
    ): Double = (random.nextGaussian() * sd(size) + mean(size)).coerceIn(size.min, size.max)

    /**
     * サイズの累積分布関数の値 (0..1) を返す。
     *
     * @param size サイズの範囲
     * @param value サイズ
     */
    fun lengthRate(
        size: FishSize,
        value: Double,
    ): Double = (1.0 + erf((value - mean(size)) / (sd(size) * sqrt(2.0)))) / 2.0

    private fun mean(size: FishSize): Double = (size.min + size.max) / 2.0

    private fun sd(size: FishSize): Double = (size.max - size.min) / RANGE_IN_SD

    private fun erf(x: Double): Double {
        val sign = if (x < 0) -1.0 else 1.0
        val absX = abs(x)
        val t = 1.0 / (1.0 + ERF_P * absX)
        val polynomial = ((((ERF_A5 * t + ERF_A4) * t + ERF_A3) * t + ERF_A2) * t + ERF_A1) * t
        return sign * (1.0 - polynomial * exp(-absX * absX))
    }
}
