package party.morino.moripafishing.core.world

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent

/**
 * Bukkit のワールド・サーバーのイベントを [WorldManagerImpl] に橋渡しする。
 */
internal class WorldEventListener(
    private val manager: WorldManagerImpl,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onWorldLoad(event: WorldLoadEvent) = manager.onBukkitWorldLoaded(event.world)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onWorldUnload(event: WorldUnloadEvent) = manager.onBukkitWorldUnloaded(event.world)

    /** 全プラグインの有効化後 (Integration の登録後) に釣りワールドの運用を始める。 */
    @EventHandler
    @Suppress("UnusedParameter") // Bukkit がイベントの型で購読先を決めるため引数が必要
    fun onServerLoad(event: ServerLoadEvent) = manager.start()
}
