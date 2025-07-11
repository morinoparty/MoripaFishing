package party.morino.moripafishing.mocks.config

import party.morino.moripafishing.api.config.PluginDirectory
import java.io.File
import java.io.InputStream

/**
 * プラグインのディレクトリを管理するモッククラス
 */
class PluginDirectoryMock : PluginDirectory {
    private val rootDirectory: File = File("src/test/resources/plugins/moripa_fishing")
    private val rarityDirectory: File = File(rootDirectory, "rarity")
    private val fishDirectory: File = File(rootDirectory, "fish")
    private val worldDirectory: File = File(rootDirectory, "world")
    private val generatorDirectory: File = File(rootDirectory, "generator")
    private val rodDirectory: File = File(rootDirectory, "rod")
    /**
     * プラグインのルートディレクトリを取得する
     * @return プラグインのルートディレクトリ
     */
    override fun getRootDirectory(): File {
        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs()
        }
        return rootDirectory
    }

    /**
     * レアリティの設定ファイルが格納されているディレクトリを取得する
     * @return レアリティの設定ファイルが格納されているディレクトリ
     */
    override fun getRarityDirectory(): File {
        if (!rarityDirectory.exists()) {
            rarityDirectory.mkdirs()
        }
        return rarityDirectory
    }

    /**
     * 魚の設定ファイルが格納されているディレクトリを取得する
     * @return 魚の設定ファイルが格納されているディレクトリ
     */
    override fun getFishDirectory(): File {
        if (!fishDirectory.exists()) {
            fishDirectory.mkdirs()
        }
        return fishDirectory
    }

    /**
     * ワールドの設定ファイルが格納されているディレクトリを取得する
     * @return ワールドの設定ファイルが格納されているディレクトリ
     */
    override fun getWorldDirectory(): File {
        if (!worldDirectory.exists()) {
            worldDirectory.mkdirs()
        }
        return worldDirectory
    }

    override fun getGeneratorDirectory(): File {
        if (!generatorDirectory.exists()) {
            generatorDirectory.mkdirs()
        }
        return generatorDirectory
    }

    override fun getRodDirectory(): File {
        if (!rodDirectory.exists()) {
            rodDirectory.mkdirs()
        }
        return rodDirectory
    }

    override fun getResource(filename: String): InputStream? {
        // リソースディレクトリからファイルを読み込む
        val resourceFile = File("bukkit/src/main/resources/$filename")
        return if (resourceFile.exists()) {
            resourceFile.inputStream()
        } else {
            null
        }
    }
}
