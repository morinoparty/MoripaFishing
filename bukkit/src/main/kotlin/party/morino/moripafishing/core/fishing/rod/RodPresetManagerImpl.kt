package party.morino.moripafishing.core.fishing.rod

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.fishing.rod.RodPresetManager
import party.morino.moripafishing.api.core.log.LogManager
import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.api.model.rod.RodPresetId
import party.morino.moripafishing.utils.Utils
import java.io.File

/**
 * プラグインディレクトリからロッドプリセットを読み込む実装
 */
class RodPresetManagerImpl : RodPresetManager, KoinComponent {
    private val pluginDirectory: PluginDirectory by inject()
    private val logManager: LogManager by inject()

    // プリセットをキャッシュする
    private val presetCache = mutableMapOf<String, RodConfiguration>()
    private var isLoaded = false

    /**
     * 指定されたIDのプリセットを取得する
     */
    override suspend fun getPreset(presetId: RodPresetId): RodConfiguration? {
        ensurePresetsLoaded()
        return presetCache[presetId.value.lowercase()]
    }

    /**
     * 利用可能なプリセットIDの一覧を取得する
     */
    override suspend fun getAllPresetIds(): List<RodPresetId> {
        ensurePresetsLoaded()
        return presetCache.keys.toList().sorted().map { RodPresetId(it) }
    }

    /**
     * プリセットが存在するかチェックする
     */
    override suspend fun hasPreset(presetId: RodPresetId): Boolean {
        ensurePresetsLoaded()
        return presetCache.containsKey(presetId.value.lowercase())
    }

    /**
     * プリセットを再読み込みする
     */
    override suspend fun reloadPresets() {
        presetCache.clear()
        isLoaded = false
        loadPresets()
        logManager.info("Rod presets reloaded. Found ${presetCache.size} presets.")
    }

    /**
     * プリセットが読み込まれていることを確認する
     */
    private suspend fun ensurePresetsLoaded() {
        if (!isLoaded) {
            loadPresets()
        }
    }

    /**
     * プラグインディレクトリまたはリソースからプリセットを読み込む
     */
    private suspend fun loadPresets() {
        val rodDirectory = pluginDirectory.getRodDirectory()

        // デフォルトプリセットのリソースファイルからpluginDirにコピー

        withContext(Dispatchers.IO) {
            copyDefaultPresetsIfNotExists(rodDirectory)
            val presetFiles = rodDirectory.listFiles { file -> file.extension == "json" }

            presetFiles?.forEach { file ->
                try {
                    val presetName = file.nameWithoutExtension
                    val jsonContent = file.readText()
                    val rodConfiguration = Utils.json.decodeFromString<RodConfiguration>(jsonContent)
                    presetCache[presetName.lowercase()] = rodConfiguration
                    logManager.info("Loaded rod preset: $presetName")
                } catch (e: Exception) {
                    logManager.severe("Failed to load rod preset: ${file.name} - ${e.message}")
                }
            }
        }
        // プラグインディレクトリからJSONファイルを読み込み
        if (presetCache.isEmpty()) {
            logManager.warning("No rod presets found in plugin directory. Please check your configuration.")
        }
        isLoaded = true
        logManager.info("Loaded ${presetCache.size} rod presets from plugin directory")
    }

    /**
     * デフォルトプリセットがない場合、リソースからコピーする
     */
    private fun copyDefaultPresetsIfNotExists(rodDirectory: File) {
        val defaultPresets = listOf("beginner", "master", "legendary", "speedster")

        defaultPresets.forEach { presetName ->
            val presetFile = File(rodDirectory, "$presetName.json")
            if (!presetFile.exists()) {
                try {
                    val resourcePath = "rod/$presetName.json"
                    val inputStream = pluginDirectory.getResource(resourcePath)

                    if (inputStream != null) {
                        inputStream.use { input ->
                            presetFile.writeText(input.bufferedReader().readText())
                        }
                        logManager.info("Copied default rod preset: $presetName")
                    } else {
                        logManager.warning("Default rod preset resource not found: $resourcePath")
                    }
                } catch (e: Exception) {
                    logManager.severe("Failed to copy default rod preset: $presetName - ${e.message}")
                }
            }
        }
    }

    /**
     * 新しいプリセットを追加する
     */
    override suspend fun addPreset(
        presetId: RodPresetId,
        configuration: RodConfiguration,
    ): Boolean {
        return try {
            val rodDirectory = pluginDirectory.getRodDirectory()
            val presetFile = File(rodDirectory, "${presetId.value.lowercase()}.json")

            // JSON形式でファイルに保存
            val jsonContent = Utils.json.encodeToString(RodConfiguration.serializer(), configuration)
            presetFile.writeText(jsonContent)

            // キャッシュにも追加
            presetCache[presetId.value.lowercase()] = configuration

            logManager.info("Added new rod preset: ${presetId.value}")
            true
        } catch (e: Exception) {
            logManager.severe("Failed to add rod preset: ${presetId.value} - ${e.message}")
            false
        }
    }
}
