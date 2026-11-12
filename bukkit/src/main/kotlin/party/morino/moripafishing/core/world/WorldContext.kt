package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import java.time.ZonedDateTime

/**
 * [FishingWorldImpl] が値を決めるために参照する環境。任意スレッドから呼ばれる。
 */
internal interface WorldContext {
    /** `WorldConfig.timeZone` における現在時刻。 */
    fun now(): ZonedDateTime

    /** ワールドごとの `climate` が `null` の場合に使う気候設定。 */
    fun defaultClimate(): ClimateConfig

    /** バニラ天候のスナップショット。 */
    fun vanillaWeather(worldKey: Key): WeatherType

    /**
     * キーが [source] の外部天候ソースによる天候の決定。
     * ソースが未登録、または例外を投げた場合は `null` (警告は実装側で出す)。
     */
    fun externalWeather(
        source: Key,
        worldId: FishingWorldId,
        worldKey: Key,
    ): WeatherDecision?
}
