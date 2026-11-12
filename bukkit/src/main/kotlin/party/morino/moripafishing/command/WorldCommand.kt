package party.morino.moripafishing.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source
import party.morino.moripafishing.api.config.world.BorderConfig
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.SpawnPoint
import party.morino.moripafishing.config.ConfigJson

/**
 * `/mf world`: 釣りワールドの一覧・移動・作成・削除・設定変更。
 *
 * @property worlds 釣りワールドの管理
 */
@Command("mf world")
internal class WorldCommand(
    private val worlds: () -> WorldManager,
) {
    @Command("list")
    @Permission("moripafishing.command.world.list")
    fun list(source: Source) {
        val sender = source.source()
        sender.sendRichMessage("<green>Fishing Worlds:")
        worlds().getWorlds().forEach { world ->
            val state = if (Bukkit.getWorld(world.getWorldKey()) != null) "loaded" else "unloaded"
            sender.sendRichMessage(
                "<green>- <name> (<id>) [$state]",
                Placeholder.component("name", Component.translatable(world.getId().toTranslateKey())),
                idOf(world.getId()),
            )
        }
    }

    @Command("transfer <world> <player>")
    @Permission("moripafishing.command.world.transfer")
    fun transfer(
        source: Source,
        @Argument("world") world: FishingWorld,
        @Argument("player") player: Player,
    ) {
        val bukkitWorld = Bukkit.getWorld(world.getWorldKey())
        if (bukkitWorld == null) {
            source.source().sendRichMessage("<red>World <id> is not loaded.", idOf(world.getId()))
            return
        }
        val spawn = world.getConfig().spawn
        player.teleportAsync(Location(bukkitWorld, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch))
    }

    @Command("create <id> <generator>")
    @Permission("moripafishing.command.world.create")
    fun create(
        source: Source,
        @Argument("id") id: String,
        @Argument(value = "generator", suggestions = "generatorIds") generator: String,
    ) {
        // 検証 (重複・不正な ID・ジェネレーター) は WorldManager が行い、失敗理由をサーバーログに出す
        val created = worlds().createWorld(FishingWorldId(id), generator)
        source.source().sendRichMessage(
            if (created) "<green>Created world <id>." else "<red>Failed to create world <id>. See the server log.",
            Placeholder.unparsed("id", id),
        )
    }

    @Suggestions("generatorIds")
    fun generatorIds(): List<String> = worlds().getGeneratorIds().sorted()

    @Command("delete <world>")
    @Permission("moripafishing.command.world.delete")
    fun delete(
        source: Source,
        @Argument("world") world: FishingWorld,
    ) {
        val deleted = worlds().deleteWorld(world.getId())
        source.source().sendRichMessage(
            if (deleted) "<green>Deleted world <id> (but folder is not deleted)." else "<red>Failed to delete world <id>.",
            idOf(world.getId()),
        )
    }

    @Command("config set spawn <world>")
    @Permission("moripafishing.command.world.config")
    fun setSpawn(
        source: PlayerSource,
        @Argument("world") world: FishingWorld,
    ) {
        val location = source.source().location
        val spawn = SpawnPoint(location.x, location.y, location.z, location.yaw, location.pitch)
        update(source, world) { it.copy(spawn = spawn) }
    }

    @Command("config set center <world> [x] [z]")
    @Permission("moripafishing.command.world.config")
    fun setCenter(
        source: Source,
        @Argument("world") world: FishingWorld,
        @Argument("x") x: Double?,
        @Argument("z") z: Double?,
    ) {
        val location = (source.source() as? Player)?.location
        val centerX = x ?: location?.x
        val centerZ = z ?: location?.z
        if (centerX == null || centerZ == null) {
            source.source().sendRichMessage("<red>x and z are required from the console.")
            return
        }
        update(source, world) {
            it.copy(border = (it.border ?: BorderConfig()).copy(centerX = centerX, centerZ = centerZ))
        }
    }

    @Command("config set size <world> <size>")
    @Permission("moripafishing.command.world.config")
    fun setSize(
        source: Source,
        @Argument("world") world: FishingWorld,
        @Argument("size") size: Double,
    ) {
        if (size <= 0.0) {
            source.source().sendRichMessage("<red>Size must be positive.")
            return
        }
        update(source, world) { it.copy(border = (it.border ?: BorderConfig()).copy(size = size)) }
    }

    @Command("config unset border <world>")
    @Permission("moripafishing.command.world.config")
    fun unsetBorder(
        source: Source,
        @Argument("world") world: FishingWorld,
    ) {
        update(source, world) { it.copy(border = null) }
    }

    @Command("config view <world>")
    @Permission("moripafishing.command.world.config")
    fun view(
        source: Source,
        @Argument("world") world: FishingWorld,
    ) {
        val sender = source.source()
        sender.sendPlainMessage(ConfigJson.encodeToString(FishingWorldConfig.serializer(), world.getConfig()))
        sender.sendPlainMessage("weather: ${world.getWeather()}")
    }

    /**
     * 最新の設定に [change] を適用して保存・適用させる。
     */
    private fun update(
        source: Source,
        world: FishingWorld,
        change: (FishingWorldConfig) -> FishingWorldConfig,
    ) {
        val updated = worlds().updateWorldConfig(change(world.getConfig()))
        source.source().sendRichMessage(
            if (updated) "<green>Updated config of world <id>." else "<red>World <id> is no longer defined.",
            idOf(world.getId()),
        )
    }
}

/** ID を MiniMessage として解釈させないプレースホルダー `<id>`。 */
private fun idOf(id: FishingWorldId): TagResolver = Placeholder.unparsed("id", id.value)
