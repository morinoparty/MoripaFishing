package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.config.climate.WeatherPolicy
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.spi.WorldState
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.world.weather.WeatherCalendar
import java.time.ZonedDateTime

/**
 * 天候の決定結果。
 *
 * @property weather 魚の抽選条件に使う天候
 * @property appliesToWorld この天候をワールドへ適用させるかどうか
 */
internal data class WeatherDecision(
    val weather: WeatherType,
    val appliesToWorld: Boolean,
)

/**
 * [FishingWorld] のコア実装。
 *
 * 設定と [WorldContext] から天候・時刻などの値を決定するだけで、Bukkit にはアクセスしない。
 * 決定した [WorldState] の適用は [WorldManagerImpl] が Integration に委ねる。
 */
internal class FishingWorldImpl(
    config: FishingWorldConfig,
    private val context: WorldContext,
) : FishingWorld {
    /** 現在の設定。書き込みは [replaceConfig] (メインスレッド) のみ。 */
    @Volatile
    private var config: FishingWorldConfig = config

    /** 天候変化イベントの判定用に、前回のリフレッシュで観測した天候。リフレッシュ (メインスレッド) のみが書き込む。 */
    @Volatile
    internal var observedWeather: WeatherType? = null

    private val worldKey: Key = WorldKeys.of(config.id)

    override fun getId(): FishingWorldId = config.id

    override fun getWorldKey(): Key = worldKey

    override fun getConfig(): FishingWorldConfig = config

    override fun getWeather(): WeatherType = decideWeather(context.now()).weather

    /** 設定を差し替える。ID は変えられない。メインスレッドから呼ぶ。 */
    internal fun replaceConfig(newConfig: FishingWorldConfig) {
        require(newConfig.id == config.id) { "world id cannot be changed: ${config.id.value} -> ${newConfig.id.value}" }
        config = newConfig
    }

    /** 指定時刻の天候と、それをワールドへ適用させるかどうかを決める。 */
    internal fun decideWeather(now: ZonedDateTime): WeatherDecision =
        when (val policy = climate().weather) {
            is WeatherPolicy.Random -> calendarDecision(policy, now)
            is WeatherPolicy.Fixed -> WeatherDecision(policy.weather, true)
            WeatherPolicy.Vanilla -> WeatherDecision(context.vanillaWeather(worldKey), false)
            is WeatherPolicy.External ->
                context.externalWeather(policy.source, config.id, worldKey)
                    ?: calendarDecision(WeatherPolicy.Random(), now)
        }

    /**
     * 指定時刻にこのワールドがあるべき状態 (全項目) を決める。
     *
     * @param now 決定の基準時刻
     * @param decision 天候の決定。決定済みの値を使い回す場合に渡す (外部ソースへの問い合わせを 1 回にするため)
     */
    internal fun desiredState(
        now: ZonedDateTime,
        decision: WeatherDecision = decideWeather(now),
    ): WorldState {
        val time = WorldClock.ticks(climate().time, now)
        return WorldState(
            worldId = config.id,
            worldKey = worldKey,
            weather = decision.weather.takeIf { decision.appliesToWorld },
            doWeatherCycle = !decision.appliesToWorld,
            time = time,
            doDaylightCycle = time == null,
            border = config.border,
            spawn = config.spawn,
        )
    }

    /** このワールドに適用される気候設定。 */
    private fun climate(): ClimateConfig = config.climate ?: context.defaultClimate()

    private fun calendarDecision(
        policy: WeatherPolicy.Random,
        now: ZonedDateTime,
    ): WeatherDecision = WeatherDecision(WeatherCalendar(policy, config.id).at(now), true)
}
