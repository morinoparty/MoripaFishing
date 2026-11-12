package party.morino.moripafishing.core.random

import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.fish.FishRandomizer
import party.morino.moripafishing.core.random.fish.FishRandomizerImpl

/**
 * RandomizeManagerの実装クラス
 */
class RandomizeManagerImpl : RandomizeManager {
    /**
     * 魚のランダム化を行うインスタンスを返す
     * @return 魚のランダム化を行うインスタンス
     */
    override fun getFishRandomizer(): FishRandomizer = FishRandomizerImpl()
}
