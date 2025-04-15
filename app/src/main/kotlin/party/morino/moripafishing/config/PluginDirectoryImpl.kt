package party.morino.moripafishing.config

import party.morino.moripafishing.api.config.PluginDirectory
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import java.io.File

/**
 * プラグインのディレクトリを管理する実装クラス
 */
class PluginDirectoryImpl : PluginDirectory, KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val _rootDirectory: File by lazy { plugin.dataFolder }
    private val _rarityDirectory: File by lazy { File(_rootDirectory, "rarity") }
    private val _fishDirectory: File by lazy { File(_rootDirectory, "fish") }

    /**
     * プラグインのルートディレクトリを取得する
     * @return プラグインのルートディレクトリ
     */
    override fun getRootDirectory(): File {
        if(!_rootDirectory.exists()) {
            _rootDirectory.mkdirs()
        }
        return _rootDirectory
    }

    /**
     * レアリティの設定ファイルが格納されているディレクトリを取得する
     * @return レアリティの設定ファイルが格納されているディレクトリ
     */
    override fun getRarityDirectory(): File {
        if(!_rarityDirectory.exists()) {
            _rarityDirectory.mkdirs()
        }
        return _rarityDirectory
    }

    /**
     * 魚の設定ファイルが格納されているディレクトリを取得する
     * @return 魚の設定ファイルが格納されているディレクトリ
     */
    override fun getFishDirectory(): File {
        if(!_fishDirectory.exists()) {
            _fishDirectory.mkdirs()
        }
        return _fishDirectory
    }
} 