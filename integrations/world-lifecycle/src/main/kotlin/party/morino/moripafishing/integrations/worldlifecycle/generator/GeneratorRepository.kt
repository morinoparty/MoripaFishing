package party.morino.moripafishing.integrations.worldlifecycle.generator

import kotlinx.serialization.json.Json
import java.io.File
import java.util.logging.Logger

/**
 * ジェネレーター定義 (`generator/` 配下の JSON ファイル) の読み込み。
 *
 * @property directory 定義ファイルのディレクトリ
 * @property logger 不正なファイルの警告の出力先
 */
internal class GeneratorRepository(
    private val directory: File,
    private val logger: Logger,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            prettyPrint = true
        }

    @Volatile
    private var generators: Map<String, GeneratorData> = emptyMap()

    /**
     * 同梱の定義 (`terra` / `void` / `normal`) が無ければ書き出し、すべての定義を読み込む。
     * 不正なファイルは警告してスキップする。
     */
    fun load() {
        directory.mkdirs()
        BUNDLED.forEach { id ->
            val file = File(directory, "$id.json")
            if (file.exists()) return@forEach
            val resource =
                GeneratorRepository::class.java.getResourceAsStream("/generator/$id.json")
                    ?: error("bundled generator resource not found: $id.json")
            resource.use { file.outputStream().use(it::copyTo) }
        }
        generators =
            directory
                .listFiles { file -> file.isFile && file.extension == "json" }
                .orEmpty()
                .mapNotNull(::read)
                .associateBy { it.id }
    }

    /** 読み込み済みのジェネレーター ID。 */
    fun ids(): Set<String> = generators.keys

    /** ID に対応する定義。未定義の場合は `null`。 */
    fun get(id: String): GeneratorData? = generators[id]

    private fun read(file: File): GeneratorData? =
        try {
            json.decodeFromString(GeneratorData.serializer(), file.readText())
        } catch (e: IllegalArgumentException) {
            logger.warning("Skipping generator ${file.name}: ${e.message}")
            null
        }

    private companion object {
        val BUNDLED = listOf("terra", "void", "normal")
    }
}
