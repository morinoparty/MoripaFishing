package party.morino.moripafishing.testing

import org.mockbukkit.mockbukkit.ServerMock
import party.morino.moripafishing.MoripaFishing
import java.io.File

/**
 * MoripaFishing をロードし、テスト用のデータフォルダ (`src/test/resources/plugins/moripa_fishing`) を
 * コピーしてから有効化する。コピー後のフィクスチャは読み取り専用として扱う。
 */
internal fun ServerMock.loadMoripaFishing(): MoripaFishing {
    val plugin = pluginManager.loadPlugin(MoripaFishing::class.java, emptyArray()) as MoripaFishing
    File("src/test/resources/plugins/moripa_fishing").copyRecursively(plugin.dataFolder, overwrite = true)
    pluginManager.enablePlugin(plugin)
    return plugin
}
