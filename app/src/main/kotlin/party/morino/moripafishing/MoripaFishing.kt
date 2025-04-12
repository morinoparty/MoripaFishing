package party.morino.moripafishing

import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.startKoin
import org.koin.dsl.module
import party.morino.moripafishing.api.random.RandomizeManager
import party.morino.moripafishing.random.RandomizeManagerImpl

class MoripaFishing : JavaPlugin() {
    override fun onEnable() {
        logger.info("MoripaFishing enabled")
        startKoin {
            modules(
                module {
                    single<RandomizeManager> { RandomizeManagerImpl() }
                }
            )
        }
    }

    override fun onDisable() {
        logger.info("MoripaFishing disabled")
    }
}