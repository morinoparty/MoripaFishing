package party.morino.moripafishing.integrations.worldlifecycle

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.plugin.java.JavaPlugin
import party.morino.moripafishing.api.core.world.lifecycle.WorldLifecycleProvider
import party.morino.moripafishing.integrations.worldlifecycle.api.GeneratorData
import party.morino.moripafishing.integrations.worldlifecycle.biome.ConstBiomeGenerator
import java.io.File

/**
 * MoripaFishing の WorldLifecycle Integration プラグイン。
 *
 * コア側の `MoripaFishing` (softdepend) が Bukkit の `PluginManager` 経由で本プラグインを
 * `WorldLifecycleProvider` として検出・利用する。
 *
 * プラグイン自体はコアに依存しないため、単体でも無害にロードされる。
 */
@OptIn(ExperimentalSerializationApi::class)
open class MoripaFishingWorldLifecyclePlugin :
    JavaPlugin(),
    WorldLifecycleProvider {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            prettyPrint = true
        }

    private val generators = mutableListOf<GeneratorData>()

    override fun onEnable() {
        loadGenerators()
        logger.info(
            "MoripaFishingWorldLifecycle enabled (generators: ${generators.map { it.id }}).",
        )
    }

    private fun loadGenerators() {
        val generatorDir = File(dataFolder, "generator")
        if (!generatorDir.exists()) {
            generatorDir.mkdirs()
        }
        val defaultIds = listOf("terra", "void", "normal")
        defaultIds.forEach { id ->
            val resource = this::class.java.getResourceAsStream("/generator/$id.json")
                ?: throw IllegalStateException("bundled generator resource not found: $id.json")
            val defaultGenerator = json.decodeFromStream<GeneratorData>(resource)
            val file = File(generatorDir, "$id.json")
            if (!file.exists()) {
                file.createNewFile()
                file.writeText(json.encodeToString(GeneratorData.serializer(), defaultGenerator))
            }
        }

        generators.clear()
        generatorDir.listFiles { f -> f.extension == "json" }?.forEach { file ->
            generators.add(json.decodeFromString(GeneratorData.serializer(), file.readText()))
        }
    }

    override fun applyBorder(
        worldId: String,
        centerX: Double,
        centerZ: Double,
        size: Double,
    ) {
        val world = Bukkit.getWorld(worldId) ?: return
        Bukkit.getScheduler().runTask(
            this,
            Runnable {
                world.worldBorder.setCenter(centerX, centerZ)
                world.worldBorder.size = size
            },
        )
    }

    override fun createBukkitWorld(
        worldId: String,
        generatorData: GeneratorData,
    ): Boolean {
        if (Bukkit.getWorld(worldId) != null) {
            return false
        }
        val reserved = setOf("world", "world_nether", "world_the_end")
        if (worldId in reserved) {
            logger.warning("World name is not allowed: $worldId")
            return false
        }
        val namespacedKey = NamespacedKey(this, worldId)
        val biomeProvider = generatorData.biomeProvider?.let { ConstBiomeGenerator(it) }
        val creator =
            WorldCreator(namespacedKey)
                .generator(generatorData.generator)
                .let { c -> generatorData.generatorSetting?.let { c.generatorSettings(it) } ?: c }
                .let { c -> generatorData.type?.let { c.type(WorldType.valueOf(it)) } ?: c }
                .biomeProvider(biomeProvider)
        val world = Bukkit.createWorld(creator)
        if (world == null) {
            logger.warning("Failed to create world $worldId")
            return false
        }
        logger.info("World ${world.name} created via integration")
        return true
    }

    override fun getGenerator(id: String): GeneratorData? = generators.find { it.id == id }

    override fun listGenerators(): List<GeneratorData> = generators.toList()

    override fun addGenerator(generator: GeneratorData) {
        generators.add(generator)
        val file = File(File(dataFolder, "generator"), "${generator.id}.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(json.encodeToString(GeneratorData.serializer(), generator))
    }
}
