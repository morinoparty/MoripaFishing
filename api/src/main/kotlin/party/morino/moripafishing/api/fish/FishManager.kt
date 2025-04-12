package party.morino.moripafishing.api.fish

/**
 * 魚の管理を行うインターフェース
 */
interface FishManager {
    /**
     * 魚を登録する
     * @param fish 登録する魚
     */
    fun registerFish(fish: Fish)

    /**
     * 魚を取得する
     * @param key 魚のキー
     * @return 魚
     */
    fun getFish(key: String): Fish?

    /**
     * 登録されている魚の一覧を取得する
     * @return 魚の一覧
     */
    fun getFishes(): List<Fish>
} 