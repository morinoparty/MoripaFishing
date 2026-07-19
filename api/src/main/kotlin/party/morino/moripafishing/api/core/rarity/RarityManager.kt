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
     * 登録されているレアリティを解除する
     * @param id 解除するレアリティのID
     * @return 解除した場合 `true`、登録されていなかった場合 `false`
     */
    fun unregisterRarity(id: RarityId): Boolean

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

    /**
     * 登録されているレアリティをすべて解除する。
     * リロード時にコアが呼び出す。`registerRarity` で登録したレアリティも破棄される点に注意。
     */
    fun unloadRarities()

    /**
     * 設定ディレクトリからレアリティの定義を読み込み登録する。
     */
    fun loadRarities()
}
