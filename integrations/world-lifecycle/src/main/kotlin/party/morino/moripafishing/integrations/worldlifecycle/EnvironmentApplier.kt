package party.morino.moripafishing.integrations.worldlifecycle

import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.World
import party.morino.moripafishing.api.core.world.spi.WorldState
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier
import party.morino.moripafishing.api.model.world.SpawnPoint

/**
 * [WorldState] のうちゲームルール `DO_DAYLIGHT_CYCLE`・時刻・ワールドボーダー・スポーンを Bukkit ワールドへ適用する。
 * メインスレッドから呼ばれる。
 */
internal class EnvironmentApplier : WorldStateApplier {
    override fun apply(state: WorldState) {
        val world = Bukkit.getWorld(state.worldKey) ?: return
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, state.doDaylightCycle)
        state.time?.let { world.time = it }
        state.border?.let {
            world.worldBorder.setCenter(it.centerX, it.centerZ)
            world.worldBorder.size = it.size
        }
        applySpawn(world, state.spawn)
    }

    /**
     * スポーンが変わる場合のみ設定する (`SpawnChangeEvent` を毎回発火させないため)。
     */
    private fun applySpawn(
        world: World,
        spawn: SpawnPoint,
    ) {
        val desired = Location(world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch)
        if (!isSameSpawn(world.spawnLocation, desired)) world.spawnLocation = desired
    }

    internal companion object {
        /**
         * ワールドのスポーンとして同じ値かどうか。
         * Paper はワールドのスポーンをブロック座標で保持するため端数は読み戻せない。pitch も保持しない版があるため、
         * ブロック座標と向き (yaw) だけを比較する。
         */
        fun isSameSpawn(
            current: Location,
            desired: Location,
        ): Boolean =
            current.blockX == desired.blockX &&
                current.blockY == desired.blockY &&
                current.blockZ == desired.blockZ &&
                current.yaw == desired.yaw
    }
}
