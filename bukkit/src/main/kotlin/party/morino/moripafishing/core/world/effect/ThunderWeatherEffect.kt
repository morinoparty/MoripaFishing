package party.morino.moripafishing.core.world.effect

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import party.morino.moripafishing.api.core.world.WeatherEffect
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.utils.coroutines.minecraft

/**
 * 雷雨の天候効果
 */
class ThunderWeatherEffect : WeatherEffect {
    override fun apply(fishingWorldId: FishingWorldId) {
        val world = Bukkit.getWorld(fishingWorldId.value)
        if (world == null) {
            return
        }
        runBlocking {
            withContext(Dispatchers.minecraft) {
                world.setStorm(true)
                world.isThundering = true
            }
        }
    }

    override fun reset() {
        // 必要なら元の状態に戻す処理を書く
    }
}
