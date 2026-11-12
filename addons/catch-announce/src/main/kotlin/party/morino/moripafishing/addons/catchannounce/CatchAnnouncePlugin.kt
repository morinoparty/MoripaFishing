package party.morino.moripafishing.addons.catchannounce

import com.charleskorn.kaml.Yaml
import org.bukkit.plugin.java.JavaPlugin
import party.morino.moripafishing.api.MoripaFishingAPIProvider
import java.io.File

/**
 * MoripaFishing の釣果通知 Addon。
 *
 * コア (`MoripaFishing`) の公開 API (`:api`) のみを利用する。コアは必須の依存で、先に有効化される。
 */
class CatchAnnouncePlugin : JavaPlugin() {
    override fun onEnable() {
        server.pluginManager.registerEvents(CatchAnnounceListener(MoripaFishingAPIProvider.get(), server, loadConfig()), this)
    }

    /** `config.yml` を読み込む。無ければ既定値で作成する。 */
    private fun loadConfig(): CatchAnnounceConfig {
        val file = File(dataFolder, "config.yml")
        if (!file.exists()) {
            dataFolder.mkdirs()
            file.writeText(Yaml.default.encodeToString(CatchAnnounceConfig.serializer(), CatchAnnounceConfig()))
        }
        return Yaml.default.decodeFromString(CatchAnnounceConfig.serializer(), file.readText())
    }
}
