package party.morino.moripafishing.api.config

/**
 * プラグインの設定 (`config.json`) の読み取り口。
 *
 * 設定の再読み込みは `/mf reload` で行う。
 */
interface ConfigManager {
    /**
     * 現在の設定 (不変スナップショット) を返す。任意のスレッドから呼べる。
     */
    fun getConfig(): ConfigData
}
