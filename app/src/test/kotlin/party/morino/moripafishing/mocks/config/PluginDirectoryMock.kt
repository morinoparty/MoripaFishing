package party.morino.moripafishing.config

import party.morino.moripafishing.api.config.PluginDirectory
import java.io.File

/**
 * プラグインのディレクトリを管理するモッククラス
 * テスト用のディレクトリ構造を提供する
 */
class PluginDirectoryMock : PluginDirectory {
    private val rootDirectory: File = File("src/test/resources/plugins/moripa_fishing")
    private val rarityDirectory: File = File(rootDirectory, "rarity")
    private val fishDirectory: File = File(rootDirectory, "fish")

    init {
        // テスト用のディレクトリを作成
        rootDirectory.mkdirs()
        rarityDirectory.mkdirs()
        fishDirectory.mkdirs()
    }

    /**
     * プラグインのルートディレクトリを取得する
     * @return プラグインのルートディレクトリ
     */
    override fun getRootDirectory(): File {
        return rootDirectory
    }

    /**
     * レアリティの設定ファイルが格納されているディレクトリを取得する
     * @return レアリティの設定ファイルが格納されているディレクトリ
     */
    override fun getRarityDirectory(): File {
        return rarityDirectory
    }

    /**
     * 魚の設定ファイルが格納されているディレクトリを取得する
     * @return 魚の設定ファイルが格納されているディレクトリ
     */
    override fun getFishDirectory(): File {
        return fishDirectory
    }
} 