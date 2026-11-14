package party.morino.moripafishing.api.core.fish

import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId

/**
 * 魚の定義を管理する。
 *
 * 定義は `fish/` 配下の JSON から読み込まれる。[register] で追加した定義は `/mf reload` で破棄される。
 * 取得系のメソッドは任意のスレッドから呼べる。
 */
interface FishManager {
    /**
     * 魚を登録する。同じ ID の魚は置き換える。
     * `displayName` は翻訳キー `moripa_fishing.fish.<id>.name` として即座に登録される (翻訳ファイルに同じキーがあればそちらを優先する)。
     *
     * @param fish 登録する魚
     * @throws IllegalArgumentException レアリティが未登録・不明な Material・不正な価値式の場合
     */
    fun register(fish: FishData)

    /**
     * 魚の登録を解除する。
     * 表示名の翻訳キーも登録を解除する。
     *
     * @param id 解除する魚の ID
     * @return 解除した場合 `true`、登録されていなかった場合 `false`
     */
    fun unregister(id: FishId): Boolean

    /**
     * 魚を返す。未登録なら `null`。
     *
     * @param id 魚の ID
     */
    fun get(id: FishId): FishData?

    /**
     * 登録されている全ての魚を返す。
     */
    fun getAll(): List<FishData>

    /**
     * 指定したレアリティの魚を返す。
     *
     * @param rarity レアリティの ID
     */
    fun getByRarity(rarity: RarityId): List<FishData>
}
