package party.morino.moripafishing.api.core.rod

import party.morino.moripafishing.api.model.rod.RodConfiguration

/**
 * ロッドプリセットの管理を行うインターフェース
 */
interface RodPresetManager {
    /**
     * 指定された名前のプリセットを取得する
     *
     * @param presetName プリセット名
     * @return プリセットのロッド設定、見つからない場合はnull
     */
    suspend fun getPreset(presetName: String): RodConfiguration?

    /**
     * 利用可能なプリセット名の一覧を取得する
     *
     * @return プリセット名のリスト
     */
    suspend fun getAllPresetNames(): List<String>

    /**
     * プリセットが存在するかチェックする
     *
     * @param presetName プリセット名
     * @return 存在する場合true
     */
    suspend fun hasPreset(presetName: String): Boolean

    /**
     * プリセットを再読み込みする
     * ファイルが変更された際などに使用
     */
    suspend fun reloadPresets()
}
