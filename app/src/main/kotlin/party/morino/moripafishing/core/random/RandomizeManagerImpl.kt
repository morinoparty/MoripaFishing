package party.morino.moripafishing.core.random

import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.fish.FishRandomizer
import party.morino.moripafishing.core.random.fish.FishRandomizerImpl
import java.util.Random

/**
 * RandomizeManagerの実装クラス
 */
class RandomizeManagerImpl : RandomizeManager {
    private val random = Random()

    /**
     * 魚のランダム化を行うインスタンスを返す
     * @return 魚のランダム化を行うインスタンス
     */
    override fun getFishRandomizer(): FishRandomizer {
        return FishRandomizerImpl()
    }
}
