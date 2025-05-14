package party.morino.moripafishing.core.world.effect

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.world.WeatherEffect
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.utils.coroutines.minecraft

/**
 * 晴れの天候効果
 */
class CloudyWeatherEffect : WeatherEffect, KoinComponent {
    private val worldManager: WorldManager by inject()
    private val configManager: ConfigManager by inject()
    private var world: World? = null
    lateinit var center: Pair<Double, Double>
    lateinit var range: IntRange

    override fun apply(fishingWorldId: FishingWorldId) {
        world = Bukkit.getWorld(fishingWorldId.value)
        if (world == null) {
            return
        }
        world as World
        // ここで、高さ200にバリアを作成
        val fishingWorld = worldManager.getWorld(fishingWorldId)
        val detailConfig = fishingWorld.getWorldDetails()
        center = detailConfig.borderCentral
        val radius = (detailConfig.borderSize ?: configManager.getConfig().world.defaultWorldSize) / 2
        val margin = 10
        range = IntRange((-radius - margin).toInt(), (radius + margin).toInt())
        runBlocking {
            withContext(Dispatchers.minecraft) {
                // start sync
                for (x in range) {
                    for (z in range) {
                        val block = world?.getBlockAt(center.first.toInt() + x, 200, center.second.toInt() + z)
                        if (block?.type == Material.AIR) {
                            block.type = Material.BARRIER
                        }
                    }
                }
                world?.setStorm(true)
                world?.isThundering = false
            }
        }
    }

    override fun reset() {
        // 必要なら元の状態に戻す処理を書く
        runBlocking {
            withContext(Dispatchers.minecraft) {
                world?.let { world ->
                    for (x in range) {
                        for (z in range) {
                            val block = world.getBlockAt(center.first.toInt() + x, 200, center.second.toInt() + z)
                            block.type = Material.AIR
                        }
                    }
                }
            }
        }
    }
}
