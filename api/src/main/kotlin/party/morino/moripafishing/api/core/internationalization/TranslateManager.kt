package party.morino.moripafishing.api.core.internationalization

/**
 * 翻訳データの管理を行うインターフェース
 */
interface TranslateManager {
    /**
     * 翻訳データを読み込む
     */
    fun load()

    /**
     * 翻訳データをリロードする
     */
    fun reload()

    /**
     * ワールドの翻訳データを読み込む
     */
    fun loadWorldData()

    /**
     * レアリティの翻訳データを読み込む
     */
    fun loadRarityData()

    /**
     * 魚の翻訳データを読み込む
     */
    fun loadFishData()
}
