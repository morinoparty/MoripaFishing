package party.morino.moripafishing.config

import java.io.File

/**
 * データフォルダ内のファイル・ディレクトリの配置。
 *
 * @property root データフォルダ
 */
internal class PluginPaths(
    val root: File,
) {
    /** 設定ファイル。 */
    val config: File get() = root.resolve("config.json")

    /** レアリティの定義を置くディレクトリ。 */
    val rarity: File get() = root.resolve("rarity")

    /** 魚の定義を置くディレクトリ。 */
    val fish: File get() = root.resolve("fish")

    /** 釣りワールドの設定を置くディレクトリ。 */
    val world: File get() = root.resolve("world")

    /** 翻訳ファイルを置くディレクトリ。 */
    val translation: File get() = root.resolve("translation")

    /**
     * データフォルダと各ディレクトリを作成する。
     */
    fun createDirectories() {
        listOf(root, rarity, fish, world, translation).forEach { it.mkdirs() }
    }
}
