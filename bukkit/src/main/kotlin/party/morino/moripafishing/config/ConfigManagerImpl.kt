package party.morino.moripafishing.config

import party.morino.moripafishing.api.config.ConfigData
import party.morino.moripafishing.api.config.ConfigManager
import java.io.File

/**
 * `config.json` を読み込む [ConfigManager] の実装。
 *
 * @property file 設定ファイル
 */
internal class ConfigManagerImpl(
    private val file: File,
) : ConfigManager {
    @Volatile
    private var config: ConfigData = ConfigData()

    /**
     * 設定ファイルを読み込む。ファイルが無いか空の場合は既定値で作成する (ディレクトリは [PluginPaths.createDirectories] で作成済みとする)。
     * 読み込みに失敗した場合は例外を投げ、以前の設定を保つ。
     *
     * @throws IllegalArgumentException 設定ファイルが不正な場合 (`SerializationException` を含む)
     */
    fun reload() {
        if (!file.exists() || file.length() == 0L) {
            file.writeText(ConfigJson.encodeToString(ConfigData.serializer(), ConfigData()))
        }
        config = ConfigJson.decodeConfig(ConfigData.serializer(), file.readText())
    }

    override fun getConfig(): ConfigData = config
}
