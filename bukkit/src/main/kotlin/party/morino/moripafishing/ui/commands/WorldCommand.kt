package party.morino.moripafishing.ui.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.world.BorderConfig
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.SpawnPoint
import party.morino.moripafishing.utils.Utils
import party.morino.moripafishing.utils.coroutines.minecraft

@Command("mf world")
@Permission("moripa_fishing.command.world")
class WorldCommand : KoinComponent {
    private val worldManager: WorldManager by inject()

    @Command("transfer <world> <player>")
    @Permission("moripa_fishing.command.world.default")
    fun transfer(
        sender: CommandSender,
        @Argument("world") world: FishingWorld,
        @Argument("player") player: Player,
    ) {
        val bukkitWorld = Bukkit.getWorld(world.getWorldKey())
        if (bukkitWorld == null) {
            sender.sendMessage("World ${world.getId().value} is not loaded.")
            return
        }
        val spawn = world.getConfig().spawn
        player.teleportAsync(Location(bukkitWorld, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch))
    }

    @Command("list")
    @Permission("moripa_fishing.command.world.default")
    fun list(sender: CommandSender) {
        sender.sendMessage("Fishing Worlds:")
        worldManager.getWorlds().forEach { world ->
            val state = if (Bukkit.getWorld(world.getWorldKey()) != null) "loaded" else "unloaded"
            sender.sendRichMessage("<green>- ${world.getId().localeTag()} (${world.getId().value}) [$state]")
        }
    }

    @Command("create <id> <generator>")
    @Permission("moripa_fishing.command.world.create")
    suspend fun create(
        sender: CommandSender,
        @Argument("id") id: String,
        @Argument(value = "generator", suggestions = "generatorIds") generator: String,
    ) {
        // 検証 (重複・不正な ID・ジェネレーター) は WorldManager が行い、失敗理由をサーバーログに出す
        val created = withContext(Dispatchers.minecraft) { worldManager.createWorld(FishingWorldId(id), generator) }
        sender.sendMessage(if (created) "Created world $id." else "Failed to create world $id. See the server log.")
    }

    @Suggestions("generatorIds")
    fun generatorIds(
        context: CommandContext<CommandSender>,
        input: String,
    ): List<String> = worldManager.getGeneratorIds().sorted()

    @Command("delete <world>")
    @Permission("moripa_fishing.command.world.delete")
    suspend fun delete(
        sender: CommandSender,
        @Argument("world") world: FishingWorld,
    ) {
        val id = world.getId().value
        val deleted = withContext(Dispatchers.minecraft) { worldManager.deleteWorld(world.getId()) }
        val message = if (deleted) "Deleted world $id (but folder is not deleted)." else "Failed to delete world $id."
        sender.sendMessage(message)
    }

    @Command("config set spawn <world>")
    @Permission("moripa_fishing.command.world.config")
    suspend fun setSpawn(
        sender: CommandSender,
        @Argument("world") world: FishingWorld,
    ) {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return
        }
        val location = sender.location
        val spawn = SpawnPoint(location.x, location.y, location.z, location.yaw, location.pitch)
        update(sender, world) { it.copy(spawn = spawn) }
    }

    @Command("config set center <world> [x] [z]")
    @Permission("moripa_fishing.command.world.config")
    suspend fun setCenter(
        sender: CommandSender,
        @Argument("world") world: FishingWorld,
        @Argument("x") x: Double? = null,
        @Argument("z") z: Double? = null,
    ) {
        val centerX = x ?: (sender as? Player)?.location?.x
        val centerZ = z ?: (sender as? Player)?.location?.z
        if (centerX == null || centerZ == null) {
            sender.sendMessage("This command can only be used by players.")
            return
        }
        update(sender, world) {
            it.copy(border = (it.border ?: BorderConfig()).copy(centerX = centerX, centerZ = centerZ))
        }
    }

    @Command("config set size <world> <size>")
    @Permission("moripa_fishing.command.world.config")
    suspend fun setSize(
        sender: CommandSender,
        @Argument("world") world: FishingWorld,
        @Argument("size") size: Double,
    ) {
        if (size <= 0.0) {
            sender.sendMessage("Size must be positive.")
            return
        }
        update(sender, world) { it.copy(border = (it.border ?: BorderConfig()).copy(size = size)) }
    }

    @Command("config unset border <world>")
    @Permission("moripa_fishing.command.world.config")
    suspend fun unsetBorder(
        sender: CommandSender,
        @Argument("world") world: FishingWorld,
    ) {
        update(sender, world) { it.copy(border = null) }
    }

    @Command("config view <world>")
    @Permission("moripa_fishing.command.world.config")
    fun view(
        sender: CommandSender,
        @Argument("world") world: FishingWorld,
    ) {
        sender.sendMessage(Utils.json.encodeToString(FishingWorldConfig.serializer(), world.getConfig()))
        sender.sendMessage("weather: ${world.getWeather()}")
    }

    /**
     * メインスレッドで最新の設定に [change] を適用して保存・適用させる。
     */
    private suspend fun update(
        sender: CommandSender,
        world: FishingWorld,
        change: (FishingWorldConfig) -> FishingWorldConfig,
    ) {
        val updated =
            withContext(Dispatchers.minecraft) {
                worldManager.updateWorldConfig(change(world.getConfig()))
            }
        val id = world.getId().value
        sender.sendMessage(if (updated) "Updated config of world $id." else "World $id is no longer defined.")
    }
}
