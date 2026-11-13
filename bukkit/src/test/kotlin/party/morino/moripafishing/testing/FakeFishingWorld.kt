package party.morino.moripafishing.testing

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.world.WorldKeys

/**
 * 固定の設定と、テストから変更できる天候を返す [FishingWorld]。
 */
internal class FakeFishingWorld(
    private val id: FishingWorldId,
    var currentWeather: WeatherType = WeatherType.SUNNY,
    private val config: FishingWorldConfig = FishingWorldConfig(id),
) : FishingWorld {
    override fun getId(): FishingWorldId = id

    override fun getWorldKey(): Key = WorldKeys.of(config)

    override fun getConfig(): FishingWorldConfig = config

    override fun getWeather(): WeatherType = currentWeather
}
