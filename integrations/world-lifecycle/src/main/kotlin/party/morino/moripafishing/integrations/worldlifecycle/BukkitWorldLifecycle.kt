package party.morino.moripafishing.integrations.worldlifecycle

import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import party.morino.moripafishing.api.core.world.spi.WorldLifecycle
import party.morino.moripafishing.integrations.worldlifecycle.biome.ConstBiomeGenerator
import party.morino.moripafishing.integrations.worldlifecycle.generator.GeneratorRepository
import java.util.logging.Logger

/**
 * [GeneratorRepository] の定義を使って Bukkit ワールドを作成・アンロードする [WorldLifecycle]。
 * [getGeneratorIds] は任意のスレッドから (読み込み済みの不変マップを返すだけ)、それ以外はメインスレッドから呼ばれる。
 *
 * @property repository ジェネレーター定義
 * @property logger ログの出力先
 */
internal class BukkitWorldLifecycle(
    private val repository: GeneratorRepository,
    private val logger: Logger,
) : WorldLifecycle {
    override fun getGeneratorIds(): Set<String> = repository.ids()

    override fun createWorld(
        worldKey: Key,
        generatorId: String,
    ): Boolean {
        val data = repository.get(generatorId)
        if (data == null || Bukkit.getWorld(worldKey) != null) return false
        val creator =
            WorldCreator(NamespacedKey(worldKey.namespace(), worldKey.value()))
                .generator(data.generator)
                .let { c -> data.generatorSetting?.let { c.generatorSettings(it) } ?: c }
                .let { c -> data.type?.let { c.type(WorldType.valueOf(it)) } ?: c }
                .biomeProvider(data.biomeProvider?.let { ConstBiomeGenerator(it) })
        val world = Bukkit.createWorld(creator)
        if (world == null) {
            logger.warning("Failed to create world ${worldKey.asString()}")
        } else {
            logger.info("Created world ${worldKey.asString()} with generator $generatorId")
        }
        return world != null
    }

    override fun unloadWorld(worldKey: Key): Boolean {
        val world = Bukkit.getWorld(worldKey) ?: return false
        return Bukkit.unloadWorld(world, false)
    }
}
