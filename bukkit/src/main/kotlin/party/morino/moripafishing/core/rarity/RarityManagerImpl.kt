package party.morino.moripafishing.core.rarity

import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.config.readDefinitions
import party.morino.moripafishing.core.fish.WorthFormula
import party.morino.moripafishing.core.internationalization.DisplayNameRegistry
import java.io.File
import java.util.logging.Logger

/**
 * `rarity/` 配下の JSON からレアリティを読み込む [RarityManager] の実装。
 *
 * 定義はコピーオンライトのマップで保持し、任意のスレッドから読める。生成時は I/O を行わない。
 *
 * @property directory レアリティの定義を置くディレクトリ
 * @property names レアリティの表示名の登録先。定義の追加・削除のたびに同期する
 * @property logger 不正なファイルの警告の出力先
 */
internal class RarityManagerImpl(
    private val directory: File,
    private val names: DisplayNameRegistry,
    private val logger: Logger,
) : RarityManager {
    @Volatile
    private var rarities: Map<RarityId, RarityData> = emptyMap()

    /**
     * 定義を読み直す。不正なファイルは警告してスキップする。[register] で追加した定義は破棄される。
     */
    @Synchronized
    fun reload() {
        val loaded =
            readDefinitions(directory, "rarity", logger, RarityData.serializer(), { it.id.value }, ::validate)
                .associateBy { it.id }
        (rarities.keys - loaded.keys).forEach { names.unregister(it.toTranslateKey()) }
        loaded.values.forEach { names.register(it.id.toTranslateKey(), it.displayName) }
        rarities = loaded
        logger.info("Loaded ${rarities.size} rarities")
    }

    @Synchronized
    override fun register(rarity: RarityData) {
        validate(rarity)
        rarities = LinkedHashMap(rarities).apply { put(rarity.id, rarity) }
        names.register(rarity.id.toTranslateKey(), rarity.displayName)
    }

    @Synchronized
    override fun unregister(id: RarityId): Boolean {
        if (id !in rarities) return false
        rarities = LinkedHashMap(rarities).apply { remove(id) }
        names.unregister(id.toTranslateKey())
        return true
    }

    override fun get(id: RarityId): RarityData? = rarities[id]

    override fun getAll(): List<RarityData> = rarities.values.toList()

    /** 価値式を検証する。不正なら `IllegalArgumentException` を投げる。 */
    private fun validate(rarity: RarityData) {
        WorthFormula(rarity.worth)
    }
}
