package party.morino.moripafishing.api.core.fishing

/**
 * ApplyValueのunitフィールドで使用する効果の種類を定義する定数
 * bonusEffectsで様々な効果を統一的に管理するために使用
 */
object EffectUnits {
    /** 待機時間に関する効果 (従来のwaitTimeMultiplier相当) */
    const val WAIT_TIME = "wait_time"

    /** 天候免疫効果 (従来のweatherImmunity相当) - 存在するだけで天候免疫が有効 */
    const val WEATHER_IMMUNITY = "weather_immunity"

    /** ワールド固有のボーナス効果 (従来のfishingWorldBonuses相当) */
    const val WORLD_BONUS = "world_bonus"

    /** 秒単位での時間効果 (エンチャント等) */
    const val SECONDS = "seconds"

    /**
     * ワールド固有のボーナス効果のunitを生成
     * @param worldName 対象ワールド名
     * @return "world:{worldName}"形式のunit文字列
     */
    fun worldBonus(worldName: String): String = "world:$worldName"

    /**
     * unitからワールド名を抽出
     * @param unit "world:{worldName}"形式の文字列
     * @return ワールド名、または該当しない場合はnull
     */
    fun extractWorldName(unit: String): String? {
        return if (unit.startsWith("world:")) {
            unit.substring(6)
        } else {
            null
        }
    }
}
