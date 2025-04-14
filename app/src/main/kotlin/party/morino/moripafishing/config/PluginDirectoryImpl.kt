package party.morino.moripafishing.config

import party.morino.moripafishing.api.config.PluginDirectory
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * プラグインのディレクトリを管理する実装クラス
 */
class PluginDirectoryImpl : PluginDirectory, KoinComponent {
    private val plugin: Plugin by inject()
    private val _rootDirectory: File by lazy { plugin.dataFolder }
    private val _rarityDirectory: File by lazy { File(_rootDirectory, "rarity") }
    private val _fishDirectory: File by lazy { File(_rootDirectory, "fish") }

    /**
     * プラグインのルートディレクトリを取得する
     * @return プラグインのルートディレクトリ
     */
    override fun getRootDirectory(): File {
        return _rootDirectory
    }

    /**
     * レアリティの設定ファイルが格納されているディレクトリを取得する
     * @return レアリティの設定ファイルが格納されているディレクトリ
     */
    override fun getRarityDirectory(): File {
        return _rarityDirectory
    }

    /**
     * 魚の設定ファイルが格納されているディレクトリを取得する
     * @return 魚の設定ファイルが格納されているディレクトリ
     */
    override fun getFishDirectory(): File {
        return _fishDirectory
    }
} 