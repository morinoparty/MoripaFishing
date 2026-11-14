package party.morino.moripafishing.listener

import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishDrawer
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.event.fishing.AnglerFishCaughtEvent
import party.morino.moripafishing.api.event.fishing.AnglerFishCaughtResultEvent
import party.morino.moripafishing.api.event.fishing.AnglerFishDrawEvent
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.api.model.fish.Fish
import party.morino.moripafishing.core.fish.FishItemFactory
import party.morino.moripafishing.core.world.toLocationData
import java.util.logging.Logger

/**
 * 釣りワールドで釣り上げた獲物を、抽選した魚のアイテムに置き換える。
 *
 * 抽選の前に [AnglerFishDrawEvent] を発火し、リスナーが変更した重みで抽選する。
 *
 * 釣りワールド外・抽選の候補なし・釣果の魚が未登録の場合はバニラのドロップを残す。
 *
 * @property anglers 釣り人の取得元
 * @property drawer 魚の抽選
 * @property fishes 釣果の魚の定義の参照先
 * @property items 魚アイテムの作成
 * @property logger 警告の出力先
 */
internal class PlayerFishingListener(
    private val anglers: AnglerManager,
    private val drawer: FishDrawer,
    private val fishes: FishManager,
    private val items: FishItemFactory,
    private val logger: Logger,
) : Listener {
    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val drop = event.caught as? Item ?: return
        val angler = anglers.getAngler(AnglerId(event.player.uniqueId))
        val world = angler.getWorld() ?: return
        val fish = draw(angler, world, event.hook.location) ?: return
        val caught = AnglerFishCaughtEvent(angler, CaughtFish.of(fish, angler.getId(), world.getId()))
        if (!caught.callEvent()) {
            event.isCancelled = true
            return
        }
        // リスナーが釣果を差し替えている場合があるため、イベントから取り直す
        val result = caught.getCaughtFish()
        val data = fishes.get(result.fish)
        if (data == null) {
            logger.warning("Caught fish '${result.fish.value}' is not registered; keeping the vanilla drop")
            return
        }
        drop.itemStack = items.create(result, data, angler.getName(), event.player.locale())
        AnglerFishCaughtResultEvent(angler, result).callEvent()
    }

    /** 抽選の候補を [AnglerFishDrawEvent] でリスナーに渡してから抽選する。 */
    private fun draw(
        angler: Angler,
        world: FishingWorld,
        hook: Location,
    ): Fish? {
        val pool = drawer.createPool(world.getId())
        AnglerFishDrawEvent(angler, world, hook.toLocationData(world.getId()), pool).callEvent()
        return drawer.draw(pool)
    }
}
