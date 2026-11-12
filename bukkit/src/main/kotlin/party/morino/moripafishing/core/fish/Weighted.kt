package party.morino.moripafishing.core.fish

import java.util.random.RandomGenerator

/**
 * 重みが 0 以下の要素を除いて重み付き抽選する。候補が無ければ `null`。
 *
 * @param random 乱数生成器
 * @param weight 要素の重み
 */
internal fun <T> List<T>.pickWeighted(
    random: RandomGenerator,
    weight: (T) -> Double,
): T? {
    val candidates = filter { weight(it) > 0.0 }
    if (candidates.isEmpty()) return null
    val target = random.nextDouble() * candidates.sumOf(weight)
    var accumulated = 0.0
    for (candidate in candidates) {
        accumulated += weight(candidate)
        if (target < accumulated) return candidate
    }
    // 浮動小数点の丸めで選ばれなかった場合
    return candidates.last()
}
