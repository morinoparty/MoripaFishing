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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.utils.coroutines.minecraft

@Command("mf")
@Permission("moripa_fishing.command.weather")
class WorldCommand: KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val worldManager : WorldManager by inject()

    @Command("world transfer <world> <player>")
    @Permission("moripa_fishing.command.world.default")
    fun transfer(source: CommandSender, @Argument("world") fishingWorld : FishingWorld,@Argument("player") player: Player) {
        val spawnPosition = fishingWorld.getWorldSpawnPosition()
        val location = Location(Bukkit.getWorld(fishingWorld.getId().value),
            spawnPosition.x,
            spawnPosition.y,
            spawnPosition.z,
            spawnPosition.yaw.toFloat(),
            spawnPosition.pitch.toFloat(),
        )
        player.teleportAsync(location)
    }

    @Command("world list")
    @Permission("moripa_fishing.command.world.default")
    fun list(sender: CommandSender) {
        sender.sendMessage("Fishing Worlds:")
        worldManager.getWorldIdList().forEach { fishingWorld ->
            val worldDetails = worldManager.getWorldDetails(fishingWorld)
            //TODO もっといい書き方
            sender.sendRichMessage("<green>${worldDetails?.name}</green>")
        }
    }

    @Command("world create <id> [generator] [biome]")
    @Permission("moripa_fishing.command.world.create")
    suspend fun create(sender: CommandSender, @Argument("id") id: String, @Argument("generator") generator: String?, @Argument("biome") biome: String?) {
        if(worldManager.getWorldIdList().contains(FishingWorldId(id))) {
            sender.sendMessage("World $id already exists.")
            return
        }
        val res = withContext(Dispatchers.minecraft){
            worldManager.createWorld(FishingWorldId(id),generator, biome)
        }
        if(!res){
            sender.sendMessage("Failed to create world $id.")
            return
        }
        sender.sendMessage("Created world ${id}.")
    }
}