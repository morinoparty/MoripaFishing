package party.morino.moripafishing.core.rod

import kotlinx.serialization.json.Json
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import party.morino.moripafishing.api.core.rod.RodPresetManager
import party.morino.moripafishing.api.model.rod.RodConfiguration
import java.io.InputStream

/**
 * リソースファイルからロッドプリセットを読み込む実装
 */
class RodPresetManagerImpl(private val plugin: Plugin) : RodPresetManager, KoinComponent {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    // プリセットをキャッシュする
    private val presetCache = mutableMapOf<String, RodConfiguration>()
    private var isLoaded = false

    /**
     * 指定された名前のプリセットを取得する
     */
    override suspend fun getPreset(presetName: String): RodConfiguration? {
        ensurePresetsLoaded()
        return presetCache[presetName.lowercase()]
    }

    /**
     * 利用可能なプリセット名の一覧を取得する
     */
    override suspend fun getAllPresetNames(): List<String> {
        ensurePresetsLoaded()
        return presetCache.keys.toList().sorted()
    }

    /**
     * プリセットが存在するかチェックする
     */
    override suspend fun hasPreset(presetName: String): Boolean {
        ensurePresetsLoaded()
        return presetCache.containsKey(presetName.lowercase())
    }

    /**
     * プリセットを再読み込みする
     */
    override suspend fun reloadPresets() {
        presetCache.clear()
        isLoaded = false
        loadPresets()
        plugin.logger.info("Rod presets reloaded. Found ${presetCache.size} presets.")
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
     * リソースからプリセットを読み込む
     */
    private suspend fun loadPresets() {
        val presetNames = listOf("beginner", "master", "legendary", "speedster")

        presetNames.forEach { presetName ->
            try {
                val resourcePath = "rod/$presetName.json"
                val inputStream: InputStream? = plugin.getResource(resourcePath)

                if (inputStream != null) {
                    val jsonContent = inputStream.bufferedReader().use { it.readText() }
                    val rodConfiguration = json.decodeFromString<RodConfiguration>(jsonContent)
                    presetCache[presetName.lowercase()] = rodConfiguration
                    plugin.logger.info("Loaded rod preset: $presetName")
                } else {
                    plugin.logger.warning("Rod preset file not found: $resourcePath")
                }
            } catch (e: Exception) {
                plugin.logger.severe("Failed to load rod preset: $presetName - ${e.message}")
            }
        }

        isLoaded = true
        plugin.logger.info("Loaded ${presetCache.size} rod presets from resources")
    }
}
