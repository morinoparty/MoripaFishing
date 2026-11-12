package party.morino.moripafishing.addons.catchannounce

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType
import java.util.UUID

/**
 * 固定の名前・ワールド・Audience を返す [Angler]。
 */
class FakeAngler(
    private val name: String,
    private val worldId: FishingWorldId?,
    private val audience: Audience = Audience.empty(),
) : Angler {
    private val id = AnglerId(UUID.randomUUID())

    override fun getId(): AnglerId = id

    override fun getName(): String = name

    override fun getWorld(): FishingWorld? =
        worldId?.let { id ->
            object : FishingWorld {
                override fun getId(): FishingWorldId = id

                override fun getWorldKey(): Key = Key.key("moripafishing", id.value)

                override fun getConfig(): FishingWorldConfig = FishingWorldConfig(id)

                override fun getWeather(): WeatherType = WeatherType.SUNNY
            }
        }

    override fun getLocation(): LocationData? = null

    override fun getAudience(): Audience = audience
}
