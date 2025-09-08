package party.morino.moripafishing.core.world

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.world.GeneratorManager
import party.morino.moripafishing.api.model.world.generator.GeneratorData
import party.morino.moripafishing.api.model.world.generator.GeneratorId
import party.morino.moripafishing.utils.Utils
import java.io.File

/**
 * GeneratorManagerの実装クラス
 * generatorディレクトリ内のjsonファイルを全て読み込む簡易実装
 */
@OptIn(ExperimentalSerializationApi::class)
class GeneratorManagerImpl :
    GeneratorManager,
    KoinComponent {
    private val pluginDirectory: PluginDirectory by inject()

    private val generators = mutableListOf<GeneratorData>()

    init {
        val default = listOf("terra", "void", "normal")
        // resources/generator内のjsonファイルを読み込む
        default.forEach { id ->
            val resource = this::class.java.getResourceAsStream("/generator/$id.json")
            if (resource == null) {
                throw IllegalStateException("generatorディレクトリが見つかりません")
            }
            val defaultGenerator = Utils.json.decodeFromStream<GeneratorData>(resource)
            val file = File(pluginDirectory.getGeneratorDirectory(), "$id.json")
            if (!file.exists()) {
                file.createNewFile()
            }
            file.writeText(Utils.json.encodeToString(defaultGenerator))
        }

        val dir = pluginDirectory.getGeneratorDirectory()
        if (!dir.exists() || !dir.isDirectory) {
            dir.mkdirs()
        }
        dir.listFiles { f -> f.extension == "json" }?.forEach { file ->
            generators.add(Utils.json.decodeFromString<GeneratorData>(file.readText()))
        }
    }

    override fun getGenerator(id: GeneratorId): GeneratorData? = generators.find { it.id == id }

    override fun getAllGenerators(): List<GeneratorData> = generators

    override fun addGenerator(generator: GeneratorData) {
        generators.add(generator)
        val file = File(pluginDirectory.getGeneratorDirectory(), "${generator.id.value}.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(Utils.json.encodeToString(generator))
    }
}
