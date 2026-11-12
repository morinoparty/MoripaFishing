package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.core.world.spi.WorldLifecycle

/** すべてのメソッドで例外を投げる [WorldLifecycle]。 */
internal class ThrowingLifecycle : WorldLifecycle {
    override fun getGeneratorIds(): Set<String> = error("generator ids are unavailable")

    override fun createWorld(
        worldKey: Key,
        generatorId: String,
    ): Boolean = error("cannot create")

    override fun unloadWorld(worldKey: Key): Boolean = error("cannot unload")
}
