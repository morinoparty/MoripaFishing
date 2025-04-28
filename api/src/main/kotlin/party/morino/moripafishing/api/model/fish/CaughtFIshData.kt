package party.morino.moripafishing.api.model.fish

import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.core.fish.CaughtFish
import java.time.ZonedDateTime

data class CaughtFishData(
    val fish: FishId,
    val size: Double,
    val rarity: RarityId,
    val worth: Double,
    val cfd: Double,
    val angler: AnglerId,
    val world: FishingWorldId,
    val timestamp: ZonedDateTime,
){
    companion object {
        fun from(caughtFish: CaughtFish): CaughtFishData {
            return CaughtFishData(
                fish = caughtFish.getId(),
                size = caughtFish.getSize(),
                rarity = caughtFish.getRarity().id,
                worth = caughtFish.getWorth(),
                cfd = caughtFish.getCFD(),
                angler = caughtFish.getAngler().getAnglerUniqueId(),
                world = caughtFish.getCaughtAtWorld().getId(),
                timestamp = caughtFish.getCaughtAt(),
            )
        }
    }
}