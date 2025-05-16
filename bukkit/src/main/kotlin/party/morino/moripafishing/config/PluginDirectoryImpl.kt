package party.morino.moripafishing.config

import java.io.File
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.config.PluginDirectory

/**
 * プラグインのディレクトリを管理する実装クラス
 */
class PluginDirectoryImpl : PluginDirectory, KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val rootDirectoryFile: File by lazy { plugin.dataFolder }
    private val rarityDirectoryFile: File by lazy { File(rootDirectoryFile, "rarity") }
    private val fishDirectoryFile: File by lazy { File(rootDirectoryFile, "fish") }
    private val worldDirectoryFile: File by lazy { File(rootDirectoryFile, "world") }
    private val generatorDirectoryFile: File by lazy { File(rootDirectoryFile, "generator") }

    /**
     * プラグインのルートディレクトリを取得する
     * @return プラグインのルートディレクトリ
     */
    override fun getRootDirectory(): File {
        if (!rootDirectoryFile.exists()) {
            rootDirectoryFile.mkdirs()
        }
        return rootDirectoryFile
    }

    /**
     * レアリティの設定ファイルが格納されているディレクトリを取得する
     * @return レアリティの設定ファイルが格納されているディレクトリ
     */
    override fun getRarityDirectory(): File {
        if (!rarityDirectoryFile.exists()) {
            rarityDirectoryFile.mkdirs()
        }
        return rarityDirectoryFile
    }

    /**
     * 魚の設定ファイルが格納されているディレクトリを取得する
     * @return 魚の設定ファイルが格納されているディレクトリ
     */
    override fun getFishDirectory(): File {
        if (!fishDirectoryFile.exists()) {
            fishDirectoryFile.mkdirs()
        }
        return fishDirectoryFile
    }

    /**
     * ワールドの設定ファイルが格納されているディレクトリを取得する
     * @return ワールドの設定ファイルが格納されているディレクトリ
     */
    override fun getWorldDirectory(): File {
        if (!worldDirectoryFile.exists()) {
            worldDirectoryFile.mkdirs()
        }
        return worldDirectoryFile
    }

    /**
     * ジェネレーターの設定ファイルが格納されているディレクトリを取得する
     * @return ジェネレーターの設定ファイルが格納されているディレクトリ
     */
    override fun getGeneratorDirectory(): File {
        if (!generatorDirectoryFile.exists()) {
            generatorDirectoryFile.mkdirs()
        }
        return generatorDirectoryFile
    }
}
