package party.morino.moripafishing.core.angler

import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.CaughtFish
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.event.fishing.FishCaughtEvent
import java.util.UUID

class AnglerImpl(
    private val uniqueId: UUID,
) : Angler, KoinComponent {
    val plugin: MoripaFishing by inject()
    val worldManager: WorldManager by inject()

    /**
     * 釣り人のIDを取得する
     * @return 釣り人のID
     */
    override fun getAnglerUniqueId(): AnglerId {
        return AnglerId(uniqueId)
    }

    override fun getMinecraftUniqueId(): UUID {
        return uniqueId
    }

    override fun getName(): String {
        // オフラインプレイヤーかもしれないので、getOfflinePlayerを使う
        // nameがnullの場合は "Unknown" を返す
        return Bukkit.getOfflinePlayer(uniqueId).name ?: "Unknown"
    }

    override fun recordCaughtFish(caughtFish: CaughtFish) {
        // 魚を釣った際にイベントを発火する
        val event = FishCaughtEvent(this, caughtFish)
        Bukkit.getPluginManager().callEvent(event)
        
        // イベントがキャンセルされていなければ記録する
        if (!event.isCancelled()) {
            plugin.logger.info("Caught fish: $caughtFish")
            // TODO databaseに記録する
        }
    }

    override fun getWorld(): FishingWorld? {
        val offlinePlayer = Bukkit.getOfflinePlayer(uniqueId)
        if (!offlinePlayer.isOnline) return null
        val player = offlinePlayer.player ?: return null
        val world = worldManager.getWorld(FishingWorldId(player.world.name))

        return world
    }
}
