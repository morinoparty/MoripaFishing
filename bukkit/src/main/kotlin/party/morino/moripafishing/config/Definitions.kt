package party.morino.moripafishing.config

import kotlinx.serialization.DeserializationStrategy
import java.io.File
import java.util.logging.Logger

/**
 * [directory] 配下 (サブディレクトリを含む) の JSON ファイルから定義を読み込む。ファイルはパス順に読み、移動しない。
 *
 * 不正な JSON・設定値の `require` 違反・[validate] の失敗・ID の重複があるファイルは警告してスキップする。
 * ID が重複した場合は先に読んだファイルを残す。
 *
 * @param directory 定義を置くディレクトリ
 * @param kind 警告に使う定義の種類 (例: `fish`)
 * @param logger 警告の出力先
 * @param deserializer 定義のデシリアライザー
 * @param idOf 重複の判定に使う定義の ID
 * @param validate 定義を検証する。問題があれば `IllegalArgumentException` を投げる
 * @return 読み込んだ定義 (読み込み順)
 */
internal fun <T> readDefinitions(
    directory: File,
    kind: String,
    logger: Logger,
    deserializer: DeserializationStrategy<T>,
    idOf: (T) -> String,
    validate: (T) -> Unit,
): List<T> {
    val definitions = LinkedHashMap<String, T>()
    directory
        .walk()
        .filter { it.isFile && it.extension == "json" }
        .sortedBy { it.path }
        .forEach { file ->
            val path = file.relativeTo(directory).invariantSeparatorsPath
            try {
                val definition = ConfigJson.decodeConfig(deserializer, file.readText())
                val id = idOf(definition)
                require(id !in definitions) { "duplicate id '$id'" }
                validate(definition)
                definitions[id] = definition
            } catch (e: IllegalArgumentException) {
                // SerializationException・require 違反・検証の失敗を含む
                logger.warning("Skipping $kind file $path: ${e.message}")
            }
        }
    return definitions.values.toList()
}
