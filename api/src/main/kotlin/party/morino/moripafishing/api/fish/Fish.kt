package party.morino.moripafishing.api.fish

/**
 * 魚を表すインターフェース
 */
interface Fish {
    /**
     * 魚のキーを取得する
     * @return 魚のキー
     */
    fun getKey(): String

    /**
     * 魚の表示名を取得する
     * @return 魚の表示名
     */
    fun getDisplayName(): String

    /**
     * 魚の長さの最小値を取得する
     * @return 魚の長さの最小値
     */
    fun getLengthMin(): Double

    /**
     * 魚の長さの最大値を取得する
     * @return 魚の長さの最大値
     */
    fun getLengthMax(): Double

    /**
     * 魚の重さを取得する
     * @return 魚の重さ
     */
    fun getWeight(): Int

    /**
     * 魚のレアリティを取得する
     * @return 魚のレアリティ
     */
    fun getRarity(): String
} 