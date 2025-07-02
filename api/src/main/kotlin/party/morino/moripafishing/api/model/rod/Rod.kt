package party.morino.moripafishing.api.model.rod

import party.morino.moripafishing.api.model.world.Location

/**
 * 釣り竿を表すクラス
 * 設定情報と釣り針の状態を管理します
 */
data class Rod(
    /**
     * ロッドの設定情報
     */
    val configuration: RodConfiguration,
    /**
     * 現在の釣り針の状態
     */
    val hook: Hook = Hook(location = null),
) {
    /**
     * 釣り針の位置を取得する
     * @return 釣り針の位置、投げられていない場合はnull
     */
    fun getHookLocation(): Location? = hook.location

    /**
     * 釣り針が水中にあるかどうかを判定する
     * @return 釣り針が水中にある場合はtrue
     */
    fun isHookInWater(): Boolean = hook.isInWater

    /**
     * 釣り針が使用可能かどうかを判定する
     * @return 釣り針が投げられている場合はtrue
     */
    fun isHookActive(): Boolean = hook.isActive()

    /**
     * 釣り針の位置を更新したRodを返す
     * @param location 新しい釣り針の位置
     * @param isInWater 釣り針が水中にあるかどうか
     * @param castTime 投げられてからの経過時間
     * @return 更新されたRod
     */
    fun updateHook(
        location: Location?,
        isInWater: Boolean = false,
        castTime: Long = 0L,
    ): Rod {
        return copy(hook = Hook(location, isInWater, castTime))
    }
}
