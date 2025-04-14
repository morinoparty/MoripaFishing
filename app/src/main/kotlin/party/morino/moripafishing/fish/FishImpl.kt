package party.morino.moripafishing.fish

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.objecthunter.exp4j.ExpressionBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.fish.Fish
import party.morino.moripafishing.api.fish.FishId
import party.morino.moripafishing.api.model.FishData
import party.morino.moripafishing.api.model.RarityData
import party.morino.moripafishing.api.rarity.RarityManager
import java.util.Locale
import kotlin.math.*


/**
 * 魚の実装クラス
 */
class FishImpl(
    private val fishData: FishData,
    private val size: Double,
) : Fish, KoinComponent {
    private val rarityManager : RarityManager by inject()
    /**
     * 魚のキーを取得する
     * @return 魚のキー
     */
    override fun getId(): FishId {
        return fishData.id
    }

    /**
     * 魚の表示名を取得する
     * @return 魚の表示名
     */
    override fun getDisplayName(): Map<Locale , Component> {
        return fishData.displayName
    }

    /**
     * 魚のサイズを取得する
     * @return 魚のサイズ
     */
    override fun getSize(): Double {
        return size
    }

    /**
     * 魚のレアリティを取得する
     * @return 魚のレアリティ
     */
    override fun getRarity(): RarityData {
        return rarityManager.getRarity(fishData.rarity) ?: throw IllegalStateException("Rarity not found: ${fishData.rarity}")
    }

    /**
     * 魚の価値を取得する
     * @return 魚の価値
     */
    override fun getWorth(): Double {
        val expression = getWorthExpression()
        val lengthRate = calculateLengthRate()
        return evaluateExpression(expression, lengthRate)
    }

    /**
     * 魚のサイズに基づいて正規分布の累積分布関数を計算する
     * @return 正規分布の累積分布関数の値
     */
    private fun calculateLengthRate(): Double {
        val (min, max) = fishData.size
        val mid = (min + max) / 2.0
        val standardDeviation = (max - min) / 6.0
        val z = (size - mid) / (standardDeviation * sqrt(2.0))
        return (1.0 + erf(z)) / 2.0
    }

    /**
     * 誤差関数（erf）を計算する
     * Abramowitz and Stegun approximationを使用
     * @param x 入力値
     * @return 誤差関数の値
     */
    private fun erf(x: Double): Double {
        val a1 = 0.254829592
        val a2 = -0.284496736
        val a3 = 1.421413741
        val a4 = -1.453152027
        val a5 = 1.061405429
        val p = 0.3275911

        val sign = if (x < 0) -1.0 else 1.0
        val absX = abs(x)

        val t = 1.0 / (1.0 + p * absX)
        val y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * exp(-absX * absX)

        return sign * y
    }

    /**
     * 式を評価する
     * @param expression 評価する式
     * @param lengthRate 魚のサイズに基づく正規分布の累積分布関数の値
     * @return 評価結果
     */
    private fun evaluateExpression(expression: String, lengthRate: Double): Double {
        val replacedExpression = expression.replace("<length_rate>", lengthRate.toString())
        return ExpressionBuilder(replacedExpression).build().evaluate()
    }

    private fun getWorthExpression(): String {
        val rarityData = rarityManager.getRarity(fishData.rarity)
        return fishData.worthExpression ?: rarityData?.worthExpression
            ?: throw IllegalStateException("Worth expression not found for fish: ${fishData.id}")
    }
} 