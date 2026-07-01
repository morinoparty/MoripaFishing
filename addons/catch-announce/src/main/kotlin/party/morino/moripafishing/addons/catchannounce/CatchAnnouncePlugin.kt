package party.morino.moripafishing.addons.catchannounce

import com.charleskorn.kaml.Yaml
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * MoripaFishing の釣果通知 Addon。
 *
 * コア (`MoripaFishing`) には依存せず、公開 API (`:api`) のみを利用して構築されている。
 * コアが導入されていないサーバーでは無害に自身を無効化する。
 */
open class CatchAnnouncePlugin : JavaPlugin() {
    private var config: CatchAnnounceConfig = CatchAnnounceConfig()

    override fun onEnable() {
        if (Bukkit.getPluginManager().getPlugin("MoripaFishing") == null) {
            logger.warning("MoripaFishing is not installed; disabling MoripaFishing-Addon-CatchAnnounce.")
            server.pluginManager.disablePlugin(this)
            return
        }
        config = loadConfig()
        server.pluginManager.registerEvents(CatchAnnounceListener(config), this)
        logger.info("MoripaFishing-Addon-CatchAnnounce enabled.")
    }

    private fun loadConfig(): CatchAnnounceConfig {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        val file = File(dataFolder, "config.yml")
        if (!file.exists()) {
            file.writeText(Yaml.default.encodeToString(CatchAnnounceConfig.serializer(), CatchAnnounceConfig()))
            return CatchAnnounceConfig()
        }
        return Yaml.default.decodeFromString(CatchAnnounceConfig.serializer(), file.readText())
    }
}
