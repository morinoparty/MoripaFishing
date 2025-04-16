package party.morino.moripafishing.core.angler

import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import java.util.*

class AnglerImpl(
    private val uniqueId : UUID,
) : Angler, KoinComponent {
    val plugin : MoripaFishing by inject()
    /**
     * 釣り人のIDを取得する
     * @return 釣り人のID
     */
    override fun getId(): AnglerId {
        return AnglerId(uniqueId)
    }

    override fun recordCaughtFish(fish: Fish) {
        plugin.logger.info("Caught fish: ${fish}")
        //TODO databaseに記録する
    }

    override fun getWorld(): FishingWorldId? {
        val offlinePlayer = Bukkit.getOfflinePlayer(uniqueId)
        if(!offlinePlayer.isOnline) return null
        val player = offlinePlayer.player ?: return null
        val world = FishingWorldId(player.world.name)
        return world
    }

} 