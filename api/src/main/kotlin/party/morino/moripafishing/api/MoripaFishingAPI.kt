package party.morino.moripafishing.api

import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.world.WorldManager

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

    /**
     * 世界マネージャーを取得する
     * @return 世界マネージャー
     */
    fun getWorldManager(): WorldManager

    /**
     * プラグインディレクトリを取得する
     * @return プラグインディレクトリ
     */
    fun getPluginDirectory(): PluginDirectory

    /**
     * 釣り人マネージャーを取得する
     * @return 釣り人マネージャー
     */
    fun getAnglerManager(): AnglerManager
}
