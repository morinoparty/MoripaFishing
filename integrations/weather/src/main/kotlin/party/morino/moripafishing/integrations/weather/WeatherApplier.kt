package party.morino.moripafishing.integrations.weather

import io.papermc.paper.math.Position
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import party.morino.moripafishing.api.core.world.spi.WorldState
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier
import party.morino.moripafishing.api.model.world.WeatherType
import java.util.UUID

/**
 * [WorldState] のうち天候とゲームルール `DO_WEATHER_CYCLE` を Bukkit ワールドへ適用する。
 *
 * すべてのメソッドはメインスレッドから呼ばれる。
 *
 * ### CLOUDY のクライアント側バリア天井
 *
 * `CLOUDY` は嵐として適用しつつ、各プレイヤーの頭上 ([CEILING_Y]) に **クライアント側だけの**
 * バリア天井を `Player.sendMultiBlockChange` で「見せる」。クライアントはプレイヤーが屋根の下に
 * いると判定し、空は暗いまま雨粒だけが当たらなくなる。実ブロックもバイオームも一切変更しないため、
 * 草・水・空の色は変化しない。
 *
 * 天井はプレイヤーに追従する小さなパッチとして [refreshCeilings] で定期的に再送され、移動やチャンク
 * 再読込にも追従する。CLOUDY を抜けたプレイヤーには本来のブロックを送り直して天井を消す。
 */
internal class WeatherApplier :
    WorldStateApplier,
    Listener {
    private companion object {
        /** バリア天井を見せる高さ。プレイヤーより十分上に置き「屋根の下」と判定させる。 */
        const val CEILING_Y = 200

        /** プレイヤーを中心に覆うパッチの水平半径 (雨の描画半径をカバーする)。 */
        const val PATCH_RADIUS = 12
    }

    private val barrierData: BlockData by lazy { Material.BARRIER.createBlockData() }

    /**
     * 本 Integration が天候を駆動しているワールドと、その天候。
     * `NamespacedKey` と Adventure の `Key` 実装はハッシュが一致しないため、キーは [normalized] で揃える。
     */
    private val driven = HashMap<Key, WeatherType>()

    /** 各プレイヤーに現在バリアとして見せているセル。CLOUDY 解除時の復元に使う。 */
    private val patches = HashMap<UUID, Set<Triple<Int, Int, Int>>>()

    override fun apply(state: WorldState) {
        val world = Bukkit.getWorld(state.worldKey) ?: return
        val key = state.worldKey.normalized()
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, state.doWeatherCycle)
        val weather = state.weather
        if (weather == null) {
            // 天候の管理をやめた: 以前に駆動していた演出だけを解除し、嵐の状態はバニラに任せる
            if (driven.remove(key) == WeatherType.CLOUDY) world.players.forEach(::clearPatch)
            return
        }
        applyStorm(world, weather)
        val previous = driven.put(key, weather)
        if (weather == WeatherType.CLOUDY) {
            world.players.forEach(::sendPatch)
        } else if (previous == WeatherType.CLOUDY) {
            world.players.forEach(::clearPatch)
        }
    }

    /**
     * CLOUDY のワールドにいるプレイヤーへ天井を再送し、そうでないプレイヤーの天井を消す。
     */
    fun refreshCeilings() {
        val cloudy = driven.filterValues { it == WeatherType.CLOUDY }.keys
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.world.key.normalized() in cloudy) {
                sendPatch(player)
            } else if (patches.containsKey(player.uniqueId)) {
                clearPatch(player)
            }
        }
    }

    /**
     * 駆動していたワールドを晴れに戻し、天井を消す。プラグイン無効化時に呼ぶ。
     * ゲームルールはコアが次回の起動時に改めて送るため、そのままにする。
     */
    fun releaseAll() {
        driven.keys.forEach { key ->
            val world = Bukkit.getWorld(key) ?: return@forEach
            world.setStorm(false)
            world.isThundering = false
            world.players.forEach(::clearPatch)
        }
        driven.clear()
    }

    /** プレイヤーに天井を見せているか。テストから状態を確認するために使う。 */
    internal fun hasCeiling(player: Player): Boolean = player.uniqueId in patches

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        patches.remove(event.player.uniqueId)
    }

    private fun applyStorm(
        world: World,
        weather: WeatherType,
    ) {
        val (storm, thunder) =
            when (weather) {
                WeatherType.THUNDERSTORM -> true to true
                WeatherType.RAINY, WeatherType.CLOUDY -> true to false
                // FOGGY / SNOWY は Bukkit の天候で表現できないため晴れとして適用する
                WeatherType.SUNNY, WeatherType.FOGGY, WeatherType.SNOWY -> false to false
            }
        world.setStorm(storm)
        world.isThundering = thunder
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

    private fun Key.normalized(): Key = Key.key(namespace(), value())
}
