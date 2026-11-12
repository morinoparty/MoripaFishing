package party.morino.moripafishing.testing

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.core.world.WorldKeys

/**
 * 与えたワールドだけを定義済みとして返す [WorldManager]。変更系の操作はすべて失敗 (`false`) を返す。
 */
internal class FakeWorldManager(
    vararg worlds: FishingWorld,
) : WorldManager {
    private val worlds = worlds.associateBy { it.getId() }

    override fun getDefaultWorldId(): FishingWorldId = FishingWorldId("default")

    override fun getWorlds(): List<FishingWorld> = worlds.values.sortedBy { it.getId().value }

    override fun getWorld(id: FishingWorldId): FishingWorld? = worlds[id]

    // NamespacedKey と Adventure の Key はハッシュが一致しないため、WorldKeys.normalize で揃えて比較する
    override fun getWorldByKey(worldKey: Key): FishingWorld? =
        worlds.values.firstOrNull { it.getWorldKey() == WorldKeys.normalize(worldKey) }

    override fun getGeneratorIds(): Set<String> = emptySet()

    override fun createWorld(
        id: FishingWorldId,
        generatorId: String,
    ): Boolean = false

    override fun deleteWorld(id: FishingWorldId): Boolean = false

    override fun updateWorldConfig(config: FishingWorldConfig): Boolean = false

    override fun reload() = Unit
}
