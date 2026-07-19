package party.morino.moripafishing.api

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.log.LogManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.core.world.weather.WeatherSource

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

    /**
     * レアリティマネージャーを取得する
     * @return レアリティマネージャー
     */
    fun getRarityManager(): RarityManager

    /**
     * ログマネージャーを取得する
     * @return ログマネージャー
     */
    fun getLogManager(): LogManager

    /**
     * 天候ソースとして [WeatherSource] を登録する。
     *
     * `ClimateConfig.weatherSource` が `source.key` と一致するワールドで使用される。
     * 同じキーで再登録した場合は上書きする。組み込みキー（`moripafishing:internal` /
     * `moripafishing:vanilla`）を上書きすることも可能だが、警告ログが出力される。
     * 登録元プラグインが無効化されると、そのプラグインが登録したソースは自動的に解除される。
     *
     * @param source 登録する天候ソース
     */
    fun registerWeatherSource(source: WeatherSource)

    /**
     * 指定キーの [WeatherSource] を解除する。登録されていない場合は何もしない。
     *
     * 解除したソースを使用中のワールドは即座に `moripafishing:internal` へフォールバックする。
     *
     * @param key 解除する天候ソースのキー
     */
    fun unregisterWeatherSource(key: Key)

    /**
     * 指定キーで登録されている [WeatherSource] を返す。未登録の場合は `null`。
     *
     * @param key 天候ソースのキー
     * @return 登録されている天候ソース、または `null`
     */
    fun getWeatherSource(key: Key): WeatherSource?

    /**
     * 現在登録されている天候ソースのキー一覧をスナップショットとして返す。
     *
     * @return 登録済みキーの集合
     */
    fun getWeatherSourceKeys(): Set<Key>
}
