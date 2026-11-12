package party.morino.moripafishing.integrations.worldlifecycle

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import party.morino.moripafishing.api.MoripaFishingAPIProvider
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.event.angler.AnglerJoinTeleportEvent

/**
 * プレイヤー参加時に釣りワールドのスポーン地点へテレポートする。
 *
 * コア (`MoripaFishing`) の API は [MoripaFishingAPIProvider] から参加ごとに取得する。
 * テレポート先はコアの設定 (`FishingWorld.getConfig().spawn`) から決め、実行前に
 * [AnglerJoinTeleportEvent] を発火して他プラグインからのキャンセルや行き先の変更を受け付ける。
 */
class JoinTeleportListener(
    private val config: JoinTeleportConfig,
) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!config.enabled) return

        val player = event.player
        if (config.onlyFirstJoin && player.hasPlayedBefore()) return

        val api = MoripaFishingAPIProvider.getOrNull() ?: return
        val worldManager = api.getWorldManager()
        val worldId = config.worldId?.let { FishingWorldId(it) } ?: worldManager.getDefaultWorldId()

        // 未定義の worldId (設定ミス等) や未ロードのワールドへはテレポートしない
        val fishingWorld = worldManager.getWorld(worldId) ?: return
        if (Bukkit.getWorld(fishingWorld.getWorldKey()) == null) return
        val spawn = fishingWorld.getConfig().spawn

        val angler = api.getAnglerManager().getAnglerByMinecraftUniqueId(player.uniqueId) ?: return
        // :api のクラスのコンストラクタは既定引数を省略せずに呼ぶ (WorldState の型の制約を参照)
        val destination =
            LocationData(fishingWorld.getId(), spawn.x, spawn.y, spawn.z, spawn.yaw.toDouble(), spawn.pitch.toDouble())
        val teleportEvent = AnglerJoinTeleportEvent(angler, destination)
        if (!teleportEvent.callEvent()) return

        val target = teleportEvent.getDestination()
        val bukkitWorld = worldManager.getWorld(target.worldId)?.getWorldKey()?.let { Bukkit.getWorld(it) } ?: return
        val location = Location(bukkitWorld, target.x, target.y, target.z, target.yaw.toFloat(), target.pitch.toFloat())
        player.teleport(location)
    }
}
