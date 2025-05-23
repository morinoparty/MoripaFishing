package party.morino.moripafishing.config

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigData
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.utils.Utils
/**
 * ConfigManagerのモッククラス
 * テスト用の設定値を提供する
 */
class ConfigManagerImpl : ConfigManager, KoinComponent {
    private lateinit var config: ConfigData
    private val pluginDirectory: PluginDirectory by inject()

    init {
        reload()
    }

    override fun reload() {
        val file = pluginDirectory.getRootDirectory().resolve("config.json")
        if (!file.exists() || file.length() == 0L) {
            file.parentFile.mkdirs()
            file.createNewFile()
            val defaultConfig = ConfigData()
            val data = Utils.json.encodeToString(ConfigData.serializer(), defaultConfig)
            file.writeText(data)
        }
        val text = file.readText()
        config = Utils.json.decodeFromString(text)
    }

    override fun getConfig(): ConfigData {
        return config
    }

    override fun applyConfig(configData: ConfigData) {
        config = configData
        val file = pluginDirectory.getRootDirectory().resolve("config.json")
        file.writeText(Utils.json.encodeToString(config))
    }
}
