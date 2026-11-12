package party.morino.moripafishing.mocks.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.world.WorldKeys

/**
 * 固定の設定と天候を返す [FishingWorld] のモック。
 */
class FishingWorldMock(
    private val id: FishingWorldId,
    private val weather: WeatherType = WeatherType.SUNNY,
    private val config: FishingWorldConfig = FishingWorldConfig(id),
) : FishingWorld {
    override fun getId(): FishingWorldId = id

    override fun getWorldKey(): Key = WorldKeys.of(id)

    override fun getConfig(): FishingWorldConfig = config

    override fun getWeather(): WeatherType = weather
}
