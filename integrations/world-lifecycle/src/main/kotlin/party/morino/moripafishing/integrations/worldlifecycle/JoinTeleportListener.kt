package party.morino.moripafishing.integrations.worldlifecycle

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.event.angler.AnglerJoinTeleportEvent

/**
 * プレイヤー参加時に釣りワールドのスポーン地点へテレポートする。
 *
 * コア本体 (`MoripaFishing`) の [MoripaFishingAPI] は参加ごとに都度取得することで、
 * コアの再読み込みなどでプラグインインスタンスが差し替わった場合にも追従する。
 * テレポート実行前に [AnglerJoinTeleportEvent] を発火し、他プラグインからの
 * キャンセルやテレポート先の変更を受け付ける。
 */
class JoinTeleportListener(
    private val config: JoinTeleportConfig,
) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!config.enabled) return

        val player = event.player
        if (config.onlyFirstJoin && player.hasPlayedBefore()) return

        val api = Bukkit.getPluginManager().getPlugin("MoripaFishing") as? MoripaFishingAPI ?: return
        val worldManager = api.getWorldManager()
        val worldId = config.worldId?.let { FishingWorldId(it) } ?: worldManager.getDefaultWorldId()

        // 登録済みの釣りワールドでない worldId (設定ミス等) は
        // getWorldDetails() が例外を投げるため、事前に弾く。
        if (worldId !in worldManager.getWorldIdList()) return
        if (Bukkit.getWorld(worldId.value) == null) return
        val spawnLocation = worldManager.getWorld(worldId).getWorldDetails().spawnLocation

        val teleportEvent = AnglerJoinTeleportEvent(player, worldId, spawnLocation)
        if (!teleportEvent.callEvent()) return

        val destination = teleportEvent.getDestination()
        val bukkitWorld = Bukkit.getWorld(destination.worldId.value) ?: return
        val location =
            Location(
                bukkitWorld,
                destination.x,
                destination.y,
                destination.z,
                destination.yaw.toFloat(),
                destination.pitch.toFloat(),
            )
        player.teleport(location)
    }
}
