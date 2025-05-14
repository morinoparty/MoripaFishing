package party.morino.moripafishing.api.core.world

import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 天候効果のインターフェース
 * 各天候ごとにapply/resetを実装する
 */
interface WeatherEffect {
    /**
     * 天候効果をワールドに適用する
     */
    fun apply(fishingWorldId: FishingWorldId)

    /**
     * 天候効果をリセット（元に戻す）
     */
    fun reset()
}
