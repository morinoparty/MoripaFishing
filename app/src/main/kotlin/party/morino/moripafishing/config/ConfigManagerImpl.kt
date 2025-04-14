package party.morino.moripafishing.config

import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigData
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import java.io.File

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
        // モックでは何もしない
        val file = pluginDirectory.getRootDirectory().resolve("config.json")
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
        }

        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            val defaultConfig = ConfigData()
            file.writeText(json.encodeToString(defaultConfig))
        }
        config =  json.decodeFromString(file.readText())
    }

    override fun getConfig(): ConfigData {
        return config
    }
}