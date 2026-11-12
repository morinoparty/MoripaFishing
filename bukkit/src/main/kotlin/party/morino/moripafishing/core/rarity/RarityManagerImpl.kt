package party.morino.moripafishing.core.rarity

import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.config.readDefinitions
import party.morino.moripafishing.core.fish.WorthFormula
import java.io.File
import java.util.logging.Logger

/**
 * `rarity/` 配下の JSON からレアリティを読み込む [RarityManager] の実装。
 *
 * 定義はコピーオンライトのマップで保持し、任意のスレッドから読める。生成時は I/O を行わない。
 *
 * @property directory レアリティの定義を置くディレクトリ
 * @property logger 不正なファイルの警告の出力先
 */
internal class RarityManagerImpl(
    private val directory: File,
    private val logger: Logger,
) : RarityManager {
    @Volatile
    private var rarities: Map<RarityId, RarityData> = emptyMap()

    /**
     * 定義を読み直す。不正なファイルは警告してスキップする。[register] で追加した定義は破棄される。
     */
    @Synchronized
    fun reload() {
        rarities =
            readDefinitions(directory, "rarity", logger, RarityData.serializer(), { it.id.value }, ::validate)
                .associateBy { it.id }
        logger.info("Loaded ${rarities.size} rarities")
    }

    @Synchronized
    override fun register(rarity: RarityData) {
        validate(rarity)
        rarities = LinkedHashMap(rarities).apply { put(rarity.id, rarity) }
    }

    @Synchronized
    override fun unregister(id: RarityId): Boolean {
        if (id !in rarities) return false
        rarities = LinkedHashMap(rarities).apply { remove(id) }
        return true
    }

    override fun get(id: RarityId): RarityData? = rarities[id]

    override fun getAll(): List<RarityData> = rarities.values.toList()

    /** 価値式を検証する。不正なら `IllegalArgumentException` を投げる。 */
    private fun validate(rarity: RarityData) {
        WorthFormula(rarity.worth)
    }
}
