package party.morino.moripafishing.listener

import org.bukkit.Bukkit
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.event.fishing.FishCaughtEvent

/**
 * プレイヤーの釣りイベントを処理するリスナー
 */
class PlayerFishingListener : Listener, KoinComponent {
    private val anglerManager: AnglerManager by inject()
    private val fishManager: FishManager by inject()
    private val worldManager: WorldManager by inject()
    private val randomizeManager: RandomizeManager by inject()

    /**
     * プレイヤーが釣りをした際のイベントハンドラ
     * CAUGHT_FISHの状態の時にFishCaughtEventを発火する
     */
    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        // 魚を釣った時のみ処理する
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return
        
        // 釣ったエンティティがアイテムでない場合は処理しない
        val caught = event.caught
        if (caught !is Item) return
        
        val player = event.player
        val angler = anglerManager.getAnglerByMinecraftUniqueId(player.uniqueId)
        
        // 釣り人が取得できない場合は処理しない
        if (angler == null) return
        
        // 釣った場所の世界を取得
        val worldId = FishingWorldId(player.world.name)
        val world = worldManager.getWorld(worldId) ?: return
        
        // ランダムな魚を生成
        val fishRandomizer = randomizeManager.getFishRandomizer()
        val fish = fishRandomizer.selectRandomFish(world.getId())
        
        // FishCaughtEventを発火
        val fishCaughtEvent = FishCaughtEvent(angler, fish)
        Bukkit.getPluginManager().callEvent(fishCaughtEvent)
        
        // イベントがキャンセルされた場合は処理を中断
        if (fishCaughtEvent.isCancelled()) return
        
        // TODO: 釣ったアイテムを魚のアイテムに置き換える処理
    }
}
