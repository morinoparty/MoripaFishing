package party.morino.moripafishing.api.config

/**
 * プラグインの設定を管理するインターフェース
 */
interface ConfigManager {
    /**
     * 設定をリロードする
     */
    fun reload()

    /**
     * 設定を取得する
     */
    fun getConfig(): ConfigData
}