package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.mockbukkit.mockbukkit.world.WorldMock

/** 指定キーの [WorldMock] を作る。 */
internal fun mockWorld(key: Key): WorldMock = WorldMock(WorldCreator(NamespacedKey(key.namespace(), key.value())))
