package party.morino.moripafishing.mocks.config

import party.morino.moripafishing.api.config.PluginDirectory
import java.io.File

/**
 * プラグインのディレクトリを管理するモッククラス
 */
class PluginDirectoryMock : PluginDirectory {
    private val _rootDirectory: File = File("src/test/resources/plugins/moripa_fishing")
    private val _rarityDirectory: File = File(_rootDirectory, "rarity")
    private val _fishDirectory: File = File(_rootDirectory, "fish")
    private val _worldDirectory: File = File(_rootDirectory, "world")

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

    /**
     * ワールドの設定ファイルが格納されているディレクトリを取得する
     * @return ワールドの設定ファイルが格納されているディレクトリ
     */
    override fun getWorldDirectory(): File {
        if(!_worldDirectory.exists()) {
            _worldDirectory.mkdirs()
        }
        return _worldDirectory
    }
} 