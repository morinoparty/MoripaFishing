package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import org.bukkit.event.world.WorldLoadEvent
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.moripafishing.api.core.world.spi.WorldLifecycle

/** MockBukkit 上でワールドを作成・アンロードする [WorldLifecycle]。 */
internal class FakeLifecycle(
    private val server: ServerMock,
    private val generators: Set<String> = setOf("void", "normal"),
) : WorldLifecycle {
    val created = mutableListOf<Pair<Key, String>>()
    var failCreate = false
    var failUnload = false

    override fun getGeneratorIds(): Set<String> = generators

    override fun createWorld(
        worldKey: Key,
        generatorId: String,
    ): Boolean {
        if (failCreate || generatorId !in generators || server.getWorld(worldKey) != null) return false
        created += worldKey to generatorId
        val world = mockWorld(worldKey)
        server.addWorld(world)
        server.pluginManager.callEvent(WorldLoadEvent(world))
        return true
    }

    override fun unloadWorld(worldKey: Key): Boolean {
        val world = server.getWorld(worldKey)
        return !failUnload && world != null && server.unloadWorld(world, false)
    }
}
