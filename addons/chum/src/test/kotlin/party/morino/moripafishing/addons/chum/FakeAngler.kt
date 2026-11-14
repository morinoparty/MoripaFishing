package party.morino.moripafishing.addons.chum

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 固定の位置と Audience を返す [Angler]。[location] が `null` なら釣りワールド外にいる。
 */
class FakeAngler(
    private val id: AnglerId,
    private val location: LocationData?,
    private val audience: Audience = Audience.empty(),
) : Angler {
    override fun getId(): AnglerId = id

    override fun getName(): String = id.value.toString()

    override fun getWorld(): FishingWorld? = location?.let { world(it.worldId) }

    override fun getLocation(): LocationData? = location

    override fun getAudience(): Audience = audience

    companion object {
        /** [id] の釣りワールド。 */
        fun world(id: FishingWorldId): FishingWorld =
            object : FishingWorld {
                override fun getId(): FishingWorldId = id

                override fun getWorldKey(): Key = Key.key("moripafishing", id.value)

                override fun getConfig(): FishingWorldConfig = FishingWorldConfig(id)

                override fun getWeather(): WeatherType = WeatherType.SUNNY
            }
    }
}
