package party.morino.moripafishing.core.fish

import org.bukkit.Material
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.config.readDefinitions
import party.morino.moripafishing.core.internationalization.DisplayNameRegistry
import java.io.File
import java.util.logging.Logger

/**
 * `fish/` 配下の JSON から魚を読み込む [FishManager] の実装。
 *
 * 定義はコピーオンライトのマップで保持し、任意のスレッドから読める。生成時は I/O を行わない。
 * レアリティを参照するため、[RarityManagerImpl][party.morino.moripafishing.core.rarity.RarityManagerImpl] の後に読み込むこと。
 *
 * @property directory 魚の定義を置くディレクトリ
 * @property rarities レアリティの存在確認に使う
 * @property names 魚の表示名の登録先。定義の追加・削除のたびに同期する
 * @property logger 不正なファイルの警告の出力先
 */
internal class FishManagerImpl(
    private val directory: File,
    private val rarities: RarityManager,
    private val names: DisplayNameRegistry,
    private val logger: Logger,
) : FishManager {
    @Volatile
    private var fishes: Map<FishId, FishData> = emptyMap()

    /**
     * 定義を読み直す。不正なファイルは警告してスキップする。[register] で追加した定義は破棄される。
     */
    @Synchronized
    fun reload() {
        val loaded =
            readDefinitions(directory, "fish", logger, FishData.serializer(), { it.id.value }, ::validate)
                .associateBy { it.id }
        (fishes.keys - loaded.keys).forEach { names.unregister(it.toTranslateKey()) }
        loaded.values.forEach { names.register(it.id.toTranslateKey(), it.displayName) }
        fishes = loaded
        logger.info("Loaded ${fishes.size} fishes")
    }

    @Synchronized
    override fun register(fish: FishData) {
        validate(fish)
        fishes = LinkedHashMap(fishes).apply { put(fish.id, fish) }
        names.register(fish.id.toTranslateKey(), fish.displayName)
    }

    @Synchronized
    override fun unregister(id: FishId): Boolean {
        if (id !in fishes) return false
        fishes = LinkedHashMap(fishes).apply { remove(id) }
        names.unregister(id.toTranslateKey())
        return true
    }

    override fun get(id: FishId): FishData? = fishes[id]

    override fun getAll(): List<FishData> = fishes.values.toList()

    override fun getByRarity(rarity: RarityId): List<FishData> = fishes.values.filter { it.rarity == rarity }

    /** レアリティ・Material・価値式を検証する。不正なら `IllegalArgumentException` を投げる。 */
    private fun validate(data: FishData) {
        require(rarities.get(data.rarity) != null) { "unknown rarity '${data.rarity.value}'" }
        val material = Material.matchMaterial(data.item.material)
        require(material != null && material.isItem) { "'${data.item.material}' is not an item material" }
        data.worth?.let(::WorthFormula)
    }
}
