package party.morino.moripafishing.listener.minecraft

import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.core.fish.BukkitFishItem
import party.morino.moripafishing.event.fishing.AnglerFishCaughtEvent
import party.morino.moripafishing.event.fishing.AnglerFishCaughtResultEvent

class PlayerFishingListener :
    Listener,
    KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val randomizerManager: RandomizeManager by inject()
    private val anglerManager: AnglerManager by inject()

    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        val player = event.player
        val state = event.state

        when (state) {
            PlayerFishEvent.State.CAUGHT_FISH -> {
                val minecraftFish = event.caught
                if (minecraftFish !is Item) {
                    return
                }
                val angler = anglerManager.getAnglerByMinecraftUniqueId(player.uniqueId) ?: return
                // 釣りワールド以外での釣りはバニラの挙動に任せる
                val anglerWorld = angler.getWorld() ?: return
                val fish = randomizerManager.getFishRandomizer().selectRandomFish(anglerWorld.getId())
                val caughtFish = CaughtFish.fromFish(fish, angler, anglerWorld)
                val fishCaughtEvent =
                    AnglerFishCaughtEvent(
                        angler,
                        caughtFish,
                    )

                fishCaughtEvent.callEvent()
                if (fishCaughtEvent.isCancelled) {
                    event.isCancelled = true
                    return
                }
                // リスナーが釣果を差し替えている場合があるため、イベントから取り直す
                val resultFish = fishCaughtEvent.getCaughtFish()
                // 未登録の FishId へ差し替えられた場合はアイテム化できないため、バニラのドロップを維持する
                val fishItem =
                    runCatching { BukkitFishItem.create(resultFish, player.locale()) }.getOrElse { e ->
                        plugin.logger.warning(
                            "Failed to create item for caught fish '${resultFish.fish.value}': ${e.message}",
                        )
                        return
                    }
                minecraftFish.itemStack = fishItem
                AnglerFishCaughtResultEvent(angler, resultFish).callEvent()
            }
            else -> {
                return
            }
        }
    }
}
