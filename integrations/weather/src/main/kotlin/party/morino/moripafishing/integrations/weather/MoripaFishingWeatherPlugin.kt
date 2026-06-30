package party.morino.moripafishing.integrations.weather

import io.papermc.paper.math.Position
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import party.morino.moripafishing.api.core.world.weather.control.WeatherControlProvider
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * MoripaFishing の Weather Integration プラグイン。
 *
 * コア側の `MoripaFishing` (softdepend) が Bukkit の `PluginManager` 経由で本プラグインを
 * `WeatherControlProvider` として検出・利用する。本プラグインが天候の破壊的適用
 * (`World.setStorm` / `isThundering`) を一手に担うことで、コア単体ではワールドを改変しない
 * 安全な構成を実現する。
 *
 * プラグイン自体はコアに依存しないため、単体でも無害にロードされる。
 *
 * `applyWeather` / `resetWeather` はコアの非同期リフレッシュループからも呼ばれるため、
 * ワールド改変は常に `BukkitScheduler.runTask` でメインスレッドへ委譲する。
 *
 * ### CLOUDY のクライアント側バリア天井
 *
 * `CLOUDY` は嵐として適用しつつ、各プレイヤーの頭上 ([CEILING_Y]) に **クライアント側だけの**
 * バリア天井を `Player.sendMultiBlockChange` で「見せる」。クライアントはプレイヤーが屋根の下に
 * いると判定し、空は暗いまま雨粒だけが当たらなくなる。実ブロックもバイオームも一切変更しないため、
 * 草・水・空の色は変化しない。
 *
 * 天井はプレイヤーに追従する小さなパッチとして [REFRESH_TICKS] ごとに再送され、移動やチャンク
 * 再読込にも追従する。CLOUDY を抜けたプレイヤーには本来のブロックを送り直して天井を消す。
 */
open class MoripaFishingWeatherPlugin :
    JavaPlugin(),
    WeatherControlProvider,
    Listener {
    companion object {
        /** バリア天井を見せる高さ。プレイヤーより十分上に置き「屋根の下」と判定させる。 */
        private const val CEILING_Y = 200

        /** プレイヤーを中心に覆うパッチの水平半径 (雨の描画半径をカバーする)。 */
        private const val PATCH_RADIUS = 12

        /** パッチ再送間隔 (tick)。 */
        private const val REFRESH_TICKS = 20L
    }

    private val barrierData: BlockData by lazy { Material.BARRIER.createBlockData() }

    /** 現在 CLOUDY を適用中のワールド ID。 */
    private val cloudyWorlds: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** 各プレイヤーに現在バリアとして見せているセル。CLOUDY 解除時の復元に使う。 */
    private val patches: ConcurrentHashMap<UUID, Set<Triple<Int, Int, Int>>> = ConcurrentHashMap()

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getScheduler().runTaskTimer(this, Runnable { refreshAll() }, REFRESH_TICKS, REFRESH_TICKS)
        logger.info("MoripaFishingWeather enabled.")
    }

    override fun applyWeather(
        worldId: String,
        weatherType: String,
    ) {
        val world = Bukkit.getWorld(worldId) ?: return
        Bukkit.getScheduler().runTask(
            this,
            Runnable {
                applyStorm(world, weatherType)
                if (weatherType == "CLOUDY") {
                    cloudyWorlds.add(worldId)
                    world.players.forEach { sendPatch(it) }
                } else if (cloudyWorlds.remove(worldId)) {
                    world.players.forEach { clearPatch(it) }
                }
            },
        )
    }

    override fun resetWeather(worldId: String) {
        val world = Bukkit.getWorld(worldId) ?: return
        Bukkit.getScheduler().runTask(
            this,
            Runnable {
                world.setStorm(false)
                world.isThundering = false
                if (cloudyWorlds.remove(worldId)) {
                    world.players.forEach { clearPatch(it) }
                }
            },
        )
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        patches.remove(event.player.uniqueId)
    }

    private fun applyStorm(
        world: World,
        weatherType: String,
    ) {
        when (weatherType) {
            "THUNDERSTORM" -> {
                world.setStorm(true)
                world.isThundering = true
            }
            "RAINY", "CLOUDY" -> {
                world.setStorm(true)
                world.isThundering = false
            }
            // SUNNY および未対応 (FOGGY / SNOWY 等) は晴れにフォールバックする
            else -> {
                world.setStorm(false)
                world.isThundering = false
            }
        }
    }

    /**
     * CLOUDY のワールドにいるプレイヤーへ天井を再送し、そうでないプレイヤーの天井を消す。
     */
    private fun refreshAll() {
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.world.name in cloudyWorlds) {
                sendPatch(player)
            } else if (patches.containsKey(player.uniqueId)) {
                clearPatch(player)
            }
        }
    }

    private fun sendPatch(player: Player) {
        val world = player.world
        val y = CEILING_Y.coerceAtMost(world.maxHeight - 1)
        val loc = player.location
        val cx = loc.blockX
        val cz = loc.blockZ
        val next =
            buildSet {
                for (x in (cx - PATCH_RADIUS)..(cx + PATCH_RADIUS)) {
                    for (z in (cz - PATCH_RADIUS)..(cz + PATCH_RADIUS)) {
                        add(Triple(x, y, z))
                    }
                }
            }
        val previous = patches[player.uniqueId].orEmpty()
        val toRestore = previous - next
        if (toRestore.isNotEmpty()) {
            player.sendMultiBlockChange(toRestore.associate { it.toPosition() to realBlockData(world, it) })
        }
        player.sendMultiBlockChange(next.associate { it.toPosition() to barrierData })
        patches[player.uniqueId] = next
    }

    private fun clearPatch(player: Player) {
        val previous = patches.remove(player.uniqueId) ?: return
        val world = player.world
        player.sendMultiBlockChange(previous.associate { it.toPosition() to realBlockData(world, it) })
    }

    private fun realBlockData(
        world: World,
        cell: Triple<Int, Int, Int>,
    ): BlockData = world.getBlockAt(cell.first, cell.second, cell.third).blockData

    private fun Triple<Int, Int, Int>.toPosition(): Position = Position.block(first, second, third)
}
