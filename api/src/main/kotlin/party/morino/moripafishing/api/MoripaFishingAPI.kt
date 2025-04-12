package party.morino.moripafishing.api

import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.random.RandomizeManager
import party.morino.moripafishing.api.fish.FishManager

/**
 * MoripaFishingのAPI
 */
interface MoripaFishingAPI {
    /**
     * 設定マネージャーを取得する
     * @return 設定マネージャー
     */
    fun getConfigManager(): ConfigManager

    /**
     * 乱数生成マネージャーを取得する
     * @return 乱数生成マネージャー
     */
    fun getRandomizeManager(): RandomizeManager

    /**
     * 魚マネージャーを取得する
     * @return 魚マネージャー
     */
    fun getFishManager(): FishManager
}

