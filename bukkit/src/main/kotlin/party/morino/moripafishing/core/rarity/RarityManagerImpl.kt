package party.morino.moripafishing.core.rarity

import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.model.rarity.RarityData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.serialization.RegexSerializer

/**
 * レアリティの管理を行う実装クラス
 */
class RarityManagerImpl : RarityManager, KoinComponent {
    private val rarities = arrayListOf<RarityData>()
    private val pluginDirectory: PluginDirectory by inject()

    init {
        loadRarities()
    }

    /**
     * レアリティを登録する
     * @param rarity 登録するレアリティ
     */
    override fun registerRarity(rarity: RarityData) {
        rarities.add(rarity)
    }

    /**
     * レアリティを取得する
     * @param id レアリティのID
     * @return レアリティ
     */
    override fun getRarity(id: RarityId): RarityData? {
        return rarities.find { it.id == id }
    }

    /**
     * 登録されているレアリティの一覧を取得する
     * @return レアリティの一覧
     */
    override fun getRarities(): List<RarityData> {
        return rarities.toList()
    }

    /**
     * レアリティを読み込む
     */
    override fun loadRarities() {
        val rarityDir = pluginDirectory.getRarityDirectory()
        if (!rarityDir.exists()) {
            rarityDir.mkdirs()
        }

        val json =
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                encodeDefaults = true
                isLenient = true
                allowStructuredMapKeys = true // Regexキーを持つMapのシリアライゼーションを有効化
                serializersModule =
                    kotlinx.serialization.modules.SerializersModule {
                        contextual(Regex::class, RegexSerializer) // Regexシリアライザーを登録
                    }
            }
        rarityDir.listFiles { file -> file.extension == "json" }?.forEach { file ->
            val rarity = json.decodeFromString<RarityData>(file.readText())
            registerRarity(rarity)
        }
    }

    override fun unloadRarities() {
        rarities.clear()
    }
}
