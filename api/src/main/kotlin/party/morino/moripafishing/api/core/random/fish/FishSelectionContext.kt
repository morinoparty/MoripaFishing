package party.morino.moripafishing.api.core.random.fish

import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.api.model.world.WeatherType
import java.time.ZonedDateTime

/**
 * 魚の選択に関するコンテキスト情報を保持するクラス
 * 魚の抽選ロジックで使用できる各種情報を提供する
 */
data class FishSelectionContext(
    /** 釣りを行うアングラー */
    val angler: Angler,
    /** 釣りを行う世界 */
    val fishingWorld: FishingWorld,
    /** 釣りを行う場所 */
    val location: Location,
    /** 現在の天気 */
    val weather: WeatherType,
    /** 釣りを行う時刻 */
    val timestamp: ZonedDateTime,
    /** 使用している釣り竿の情報（オプション） */
    val rodInfo: Map<String, Any> = emptyMap(),
    /** 追加のカスタム情報（オプション） */
    val customData: Map<String, Any> = emptyMap(),
) {
    /**
     * 現在の時刻（時間）を取得する
     * @return 0-23の時間
     */
    fun getCurrentHour(): Int = timestamp.hour

    /**
     * 現在の日付（日）を取得する
     * @return 1-31の日
     */
    fun getCurrentDay(): Int = timestamp.dayOfMonth

    /**
     * 現在の月を取得する
     * @return 1-12の月
     */
    fun getCurrentMonth(): Int = timestamp.monthValue

    /**
     * 現在の曜日を取得する
     * @return 1-7の曜日（月曜=1, 日曜=7）
     */
    fun getCurrentDayOfWeek(): Int = timestamp.dayOfWeek.value

    /**
     * 昼間かどうかを判定する
     * @return 昼間の場合true
     */
    fun isDaytime(): Boolean = getCurrentHour() in 6..17

    /**
     * 夜間かどうかを判定する
     * @return 夜間の場合true
     */
    fun isNighttime(): Boolean = !isDaytime()

    /**
     * 雨天かどうかを判定する
     * @return 雨天の場合true
     */
    fun isRaining(): Boolean = weather.name.lowercase().contains("rain")

    /**
     * 晴天かどうかを判定する
     * @return 晴天の場合true
     */
    fun isSunny(): Boolean = weather.name.lowercase().contains("sunny")
}
