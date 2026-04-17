package party.morino.moripafishing.api

import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.log.LogManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.core.world.lifecycle.WorldLifecycleProvider
import party.morino.moripafishing.api.core.world.weather.WeatherProvider
import party.morino.moripafishing.api.model.world.FishingWorldId

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
     * 指定ワールドの天候ソースとして外部 `WeatherProvider` を登録する。
     *
     * `ClimateConfig.weatherMode` が `EXTERNAL` のワールドに対してのみ効果を持ち、
     * `INTERNAL` / `VANILLA` モードのワールドでは登録しても参照されない。
     *
     * @param worldId 対象ワールドの ID
     * @param provider 天候を提供する `WeatherProvider`
     */
    fun registerWeatherProvider(
        worldId: FishingWorldId,
        provider: WeatherProvider,
    )

    /**
     * 指定ワールドに登録されていた外部 `WeatherProvider` を解除する。
     * 登録されていない場合は何もしない。
     *
     * @param worldId 対象ワールドの ID
     */
    fun unregisterWeatherProvider(worldId: FishingWorldId)

    /**
     * `WorldLifecycleProvider` (Integration) を取得する。
     *
     * ワールド境界の同期やカスタムジェネレーターでのワールド作成を担当する外部 jar
     * (`MoripaFishingWorldLifecycle` 等) が存在する場合に返る。
     * 未導入のサーバーでは `null` を返し、該当機能はスキップされる。
     *
     * @return 登録されたプロバイダー、未導入時は `null`
     */
    fun getWorldLifecycleProvider(): WorldLifecycleProvider?
}
