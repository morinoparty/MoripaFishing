package party.morino.moripafishing.api.core.rarity

import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId

/**
 * レアリティの管理を行うインターフェース
 */
interface RarityManager {
    /**
     * レアリティを登録する
     * @param rarity 登録するレアリティ
     */
    fun registerRarity(rarity: RarityData)

    /**
     * レアリティを取得する
     * @param id レアリティのID
     * @return レアリティ
     */
    fun getRarity(id: RarityId): RarityData?

    /**
     * 登録されているレアリティの一覧を取得する
     * @return レアリティの一覧
     */
    fun getRarities(): List<RarityData>
}
