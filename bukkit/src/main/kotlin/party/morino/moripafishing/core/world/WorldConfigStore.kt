package party.morino.moripafishing.core.world

import kotlinx.serialization.json.Json
import net.kyori.adventure.key.InvalidKeyException
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.config.ConfigJson
import party.morino.moripafishing.config.decodeConfig
import java.io.File
import java.util.logging.Logger

/**
 * 釣りワールドの設定ファイル (`world/<id>.json`) の読み書き。
 *
 * @property directory 設定ファイルを置くディレクトリ
 * @property logger 不正なファイルの警告の出力先
 * @property json 読み書きに使う JSON 設定
 */
internal class WorldConfigStore(
    private val directory: File,
    private val logger: Logger,
    private val json: Json = ConfigJson,
) {
    /**
     * すべての設定ファイルを読み込む。
     * 不正な JSON・ファイル名と `id` の不一致・Bukkit ワールドのキーにできない `id` のファイルは警告してスキップする。
     */
    fun loadAll(): List<FishingWorldConfig> =
        directory
            .listFiles { file -> file.isFile && file.extension == "json" }
            .orEmpty()
            .sortedBy { it.name }
            .mapNotNull(::load)

    /**
     * 設定を `<id>.json` に保存する。
     */
    fun save(config: FishingWorldConfig) {
        directory.mkdirs()
        fileOf(config.id).writeText(json.encodeToString(FishingWorldConfig.serializer(), config))
    }

    /**
     * 設定ファイルを削除する。存在しない場合は何もしない。
     */
    fun delete(id: FishingWorldId) {
        fileOf(id).delete()
    }

    private fun load(file: File): FishingWorldConfig? {
        val config =
            try {
                json.decodeConfig(FishingWorldConfig.serializer(), file.readText())
            } catch (e: IllegalArgumentException) {
                // SerializationException と設定値の require 違反の両方を含む
                logger.warning("Skipping world config ${file.name}: ${e.message}")
                return null
            }
        val problem =
            when {
                config.id.value != file.nameWithoutExtension -> "id '${config.id.value}' does not match the file name"
                !isValidKey(config.id) -> "'${config.id.value}' is not a valid world key value"
                else -> null
            }
        problem?.let { logger.warning("Skipping world config ${file.name}: $it") }
        return config.takeIf { problem == null }
    }

    private fun isValidKey(id: FishingWorldId): Boolean =
        try {
            WorldKeys.of(id)
            true
        } catch (_: InvalidKeyException) {
            false
        }

    private fun fileOf(id: FishingWorldId): File = File(directory, "${id.value}.json")
}
