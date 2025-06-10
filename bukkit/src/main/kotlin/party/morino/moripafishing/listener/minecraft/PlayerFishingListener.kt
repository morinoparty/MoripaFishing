package party.morino.moripafishing.listener.minecraft

import BukkitFishItem
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fishing.FishingManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.core.fishing.WaitTimeManagerImpl
import party.morino.moripafishing.event.fishing.AnglerFishCaughtEvent
import party.morino.moripafishing.utils.rod.RodAnalyzer

class PlayerFishingListener(private val plugin: Plugin) : Listener, KoinComponent {
    val randomizerManager: RandomizeManager by inject()
    val worldManager: WorldManager by inject()
    val angerManager: AnglerManager by inject()
    val fishingManager: FishingManager by inject()

    private val rodAnalyzer = RodAnalyzer(plugin)

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

                // 釣竿の解析と効果適用
                analyzeAndApplyRodEffects(player, angler)

                val waitTime = fishingManager.getWaitTimeManager().getWaitTime(angler)
                fishingHook.isSkyInfluenced = false
                fishingHook.isRainInfluenced = false
                val minTick = (waitTime.first * 20).toInt() // 最小待機時間（ティック単位）
                val maxTick = (waitTime.second * 20).toInt() // 最大待機時間（ティック単位）
                fishingHook.setWaitTime(minTick, maxTick)
            }

            PlayerFishEvent.State.CAUGHT_ENTITY,
            PlayerFishEvent.State.FAILED_ATTEMPT,
            PlayerFishEvent.State.REEL_IN,
            -> {
                // 釣り終了時に釣り人につけた効果をクリア
                val angler = angerManager.getAnglerByMinecraftUniqueId(player.uniqueId) ?: return
                val waitTimeManager = fishingManager.getWaitTimeManager() as? WaitTimeManagerImpl
                waitTimeManager?.clearAnglerEffects(angler.getAnglerUniqueId())
            }

            else -> {
                return
            }
        }
    }

    private fun analyzeAndApplyRodEffects(
        player: org.bukkit.entity.Player,
        angler: party.morino.moripafishing.api.core.angler.Angler,
    ) {
        val heldItem = player.inventory.itemInMainHand
        val waitTimeManager = fishingManager.getWaitTimeManager()

        // カスタム釣竿（PDC）の解析
        val customRodConfig = rodAnalyzer.analyzeRod(heldItem)
        if (customRodConfig != null) {
            applyRodConfiguration(angler, customRodConfig, waitTimeManager)
            return
        }

        // バニラ釣竿（エンチャント付き）の解析
        val vanillaRodConfig = rodAnalyzer.analyzeVanillaRod(heldItem)
        if (vanillaRodConfig != null) {
            applyRodConfiguration(angler, vanillaRodConfig, waitTimeManager)
        }
    }

    private fun applyRodConfiguration(
        angler: party.morino.moripafishing.api.core.angler.Angler,
        rodConfig: party.morino.moripafishing.api.model.rod.RodConfiguration,
        waitTimeManager: party.morino.moripafishing.api.core.fishing.WaitTimeManager,
    ) {
        val effects = rodAnalyzer.getAllEffects(rodConfig)
        val effectDuration = 300000L // 5分間有効

        for (effect in effects) {
            waitTimeManager.applyForAngler(angler.getAnglerUniqueId(), effect, effectDuration)
        }
    }
}
