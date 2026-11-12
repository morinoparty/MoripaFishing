package party.morino.moripafishing.integrations.weather

import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier

/**
 * MoripaFishing の Weather Integration プラグイン。
 *
 * コア (`MoripaFishing`) に依存してその後にロードされ、[WeatherApplier] を `WorldStateApplier` として
 * `ServicesManager` に登録する。コアが決定した天候とゲームルール `ADVANCE_WEATHER` を Bukkit ワールドへ適用する。
 * 登録は本プラグインの無効化時に自動で解除される。
 */
open class MoripaFishingWeatherPlugin : JavaPlugin() {
    private lateinit var applier: WeatherApplier

    override fun onEnable() {
        applier = WeatherApplier()
        server.pluginManager.registerEvents(applier, this)
        server.scheduler.runTaskTimer(
            this,
            Runnable { applier.refreshCeilings() },
            CEILING_REFRESH_TICKS,
            CEILING_REFRESH_TICKS,
        )
        server.servicesManager.register(WorldStateApplier::class.java, applier, this, ServicePriority.Normal)
        logger.info("MoripaFishing-Integration-Weather enabled.")
    }

    override fun onDisable() {
        if (::applier.isInitialized) applier.releaseAll()
    }

    private companion object {
        /** CLOUDY の天井パッチを再送する間隔 (tick)。 */
        const val CEILING_REFRESH_TICKS = 20L
    }
}
