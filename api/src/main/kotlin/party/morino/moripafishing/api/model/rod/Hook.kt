package party.morino.moripafishing.api.model.rod

import party.morino.moripafishing.api.model.world.Location

/**
 * 釣り針を表すデータクラス
 * 釣り針の位置情報と状態を管理します
 */
data class Hook(
    /**
     * 釣り針の現在位置
     * 釣りをしていない場合はnull
     */
    val location: Location?,
    /**
     * 釣り針が水中にあるかどうか
     */
    val isInWater: Boolean = false,
    /**
     * 釣り針が投げられてからの経過時間（ミリ秒）
     */
    val castTime: Long = 0L,
) {
    /**
     * 釣り針が使用可能かどうかを判定する
     * @return 釣り針の位置が設定されている場合はtrue
     */
    fun isActive(): Boolean = location != null
}
