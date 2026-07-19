package party.morino.moripafishing.api.core.fish

import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId

/**
 * 魚の管理を行うインターフェース
 */
interface FishManager {
    /**
     * 魚を登録する
     * @param fish 登録する魚
     */
    fun registerFish(fish: FishData)

    /**
     * 登録されている魚を解除する
     * @param id 解除する魚のID
     * @return 解除した場合 `true`、登録されていなかった場合 `false`
     */
    fun unregisterFish(id: FishId): Boolean

    /**
     * 登録されている魚の一覧を取得する
     * @return 魚の一覧
     */
    fun getFish(): List<FishData>

    /**
     * 魚を取得する
     * @param id 魚のID
     * @return 魚
     */
    fun getFishWithId(id: FishId): FishData?

    /**
     * 魚を取得する
     * @param rarity レアリティ
     * @return 魚
     */
    fun getFishesWithRarity(rarity: RarityId): List<FishData>

    /**
     * 登録されている魚をすべて解除する。
     * リロード時にコアが呼び出す。`registerFish` で登録した魚も破棄される点に注意。
     */
    fun unloadFishes()

    /**
     * 設定ディレクトリから魚の定義を読み込み登録する。
     */
    fun loadFishes()
}
