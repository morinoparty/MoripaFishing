package party.morino.moripafishing.core.fish

import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.serialization.RegexSerializer
import java.io.File

/**
 * 魚の管理を行う実装クラス
 */
class FishManagerImpl : FishManager, KoinComponent {
    // 魚のデータを保持するマップ
    private val fishes = mutableMapOf<FishId, FishData>()

    // プラグインディレクトリのインスタンスを注入
    private val pluginDirectory: PluginDirectory by inject()

    init {
        // 初期化時に魚のデータを読み込む
        loadFishes()
    }

    /**
     * 魚を登録する
     * @param fish 登録する魚
     */
    override fun registerFish(fish: FishData) {
        // 魚のIDをキーにしてマップに登録
        fishes[fish.id] = fish
    }

    /**
     * 登録されている魚の一覧を取得する
     * @return 魚の一覧
     */
    override fun getFish(): List<FishData> {
        return fishes.values.toList()
    }

    /**
     * 魚を取得する
     * @param id 魚のID
     * @return 魚
     */
    override fun getFishWithId(id: FishId): FishData? {
        return fishes[id]
    }

    /**
     * レアリティに基づいて魚を取得する
     * @param rarity レアリティ
     * @return 魚の一覧
     */
    override fun getFishesWithRarity(rarity: RarityId): List<FishData> {
        return fishes.values.filter { it.rarity == rarity }
    }

    /**
     * 魚のデータを読み込む
     * 管理しやすいようにfish/<rarity>/<fish_name>.jsonに移動
     */
    override fun loadFishes() {
        val fishDir = pluginDirectory.getFishDirectory()
        if (!fishDir.exists()) {
            fishDir.mkdirs()
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
        // ルートディレクトリのファイルを処理
        processFishFiles(fishDir, json)

        // レアリティごとのディレクトリのファイルを処理
        fishDir.listFiles { file -> file.isDirectory }?.forEach { rarityDir ->
            processFishFiles(rarityDir, json)
        }
    }

    override fun unloadFishes() {
        fishes.clear()
    }

    /**
     * 指定されたディレクトリ内の魚の設定ファイルを処理する
     * @param directory 処理するディレクトリ
     * @param json JSONパーサー
     */
    private fun processFishFiles(
        directory: File,
        json: Json,
    ) {
        directory.listFiles { file -> file.extension == "json" }?.forEach { file ->
            val fish = json.decodeFromString<FishData>(file.readText())
            registerFish(fish)

            // ファイルが正しいディレクトリにない場合は移動
            val correctDir = File(pluginDirectory.getFishDirectory(), fish.rarity.value)
            if (file.parentFile != correctDir) {
                if (!correctDir.exists()) {
                    correctDir.mkdirs()
                }

                val newFile = File(correctDir, file.name)
                if (!newFile.exists()) {
                    file.renameTo(newFile)
                }
            }
        }
    }
}
