package party.morino.moripafishing.api.core.rarity

import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId

/**
 * レアリティの定義を管理する。
 *
 * 定義は `rarity/` 配下の JSON から読み込まれる。[register] で追加した定義は `/mf reload` で破棄される。
 * 取得系のメソッドは任意のスレッドから呼べる。
 */
interface RarityManager {
    /**
     * レアリティを登録する。同じ ID のレアリティは置き換える。
     *
     * @param rarity 登録するレアリティ
     * @throws IllegalArgumentException 不正な価値式の場合
     */
    fun register(rarity: RarityData)

    /**
     * レアリティの登録を解除する。
     *
     * @param id 解除するレアリティの ID
     * @return 解除した場合 `true`、登録されていなかった場合 `false`
     */
    fun unregister(id: RarityId): Boolean

    /**
     * レアリティを返す。未登録なら `null`。
     *
     * @param id レアリティの ID
     */
    fun get(id: RarityId): RarityData?

    /**
     * 登録されている全てのレアリティを返す。
     */
    fun getAll(): List<RarityData>
}
