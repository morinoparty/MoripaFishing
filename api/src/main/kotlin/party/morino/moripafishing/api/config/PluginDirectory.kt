package party.morino.moripafishing.api.config

import java.io.File

/**
 * プラグインのディレクトリを管理するインターフェース
 */
interface PluginDirectory {
    /**
     * プラグインのルートディレクトリを取得する
     * @return プラグインのルートディレクトリ
     */
    fun getRootDirectory(): File

    /**
     * レアリティの設定ファイルが格納されているディレクトリを取得する
     * @return レアリティの設定ファイルが格納されているディレクトリ
     */
    fun getRarityDirectory(): File

    /**
     * 魚の設定ファイルが格納されているディレクトリを取得する
     * @return 魚の設定ファイルが格納されているディレクトリ
     */
    fun getFishDirectory(): File

    /**
     * ワールドの設定ファイルが格納されているディレクトリを取得する
     * @return ワールドの設定ファイルが格納されているディレクトリ
     */
    fun getWorldDirectory(): File
} 