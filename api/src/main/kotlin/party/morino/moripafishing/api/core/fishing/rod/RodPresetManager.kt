package party.morino.moripafishing.api.core.fishing.rod

import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.api.model.rod.RodPresetId

/**
 * ロッドプリセットの管理を行うインターフェース
 */
interface RodPresetManager {
    /**
     * 指定されたIDのプリセットを取得する
     *
     * @param presetId プリセットID
     * @return プリセットのロッド設定、見つからない場合はnull
     */
    suspend fun getPreset(presetId: RodPresetId): RodConfiguration?

    /**
     * 利用可能なプリセットIDの一覧を取得する
     *
     * @return プリセットIDのリスト
     */
    suspend fun getAllPresetIds(): List<RodPresetId>

    /**
     * プリセットが存在するかチェックする
     *
     * @param presetId プリセットID
     * @return 存在する場合true
     */
    suspend fun hasPreset(presetId: RodPresetId): Boolean

    /**
     * プリセットを再読み込みする
     * ファイルが変更された際などに使用
     */
    suspend fun reloadPresets()

    /**
     * 新しいプリセットを追加する
     *
     * @param presetId プリセットID
     * @param configuration ロッド設定
     * @return 追加に成功した場合true
     */
    suspend fun addPreset(
        presetId: RodPresetId,
        configuration: RodConfiguration,
    ): Boolean
}
