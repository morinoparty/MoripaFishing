package party.morino.moripafishing.listener.minecraft

import BukkitFishItem
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fishing.FishingManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.event.fishing.AnglerFishCaughtEvent

class PlayerFishingListener : Listener, KoinComponent {
    val randomizerManager: RandomizeManager by inject()
    val worldManager: WorldManager by inject()
    val angerManager: AnglerManager by inject()
    val fishingManager: FishingManager by inject()

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
                val angler = angerManager.getAnglerByMinecraftUniqueId(player.uniqueId) ?: return
                val anglerWorld = angler.getWorld() ?: return
                val fish = randomizerManager.getFishRandomizer().selectRandomFish(anglerWorld.getId())
                val caughtFish = CaughtFish.Companion.fromFish(fish, angler, anglerWorld)
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
                val fishItem = BukkitFishItem.create(caughtFish)
                minecraftFish.itemStack = fishItem
            }

            PlayerFishEvent.State.FISHING -> {
                val fishingHook = event.hook
                val angler = angerManager.getAnglerByMinecraftUniqueId(player.uniqueId) ?: return
                val waitTime = fishingManager.getWaitTimeManager().getWaitTime(angler)
                fishingHook.isSkyInfluenced = false
                fishingHook.isRainInfluenced = false
                fishingHook.setWaitTime(waitTime.first, waitTime.second)
            }

            else -> {
                return
            }
        }
    }
}
