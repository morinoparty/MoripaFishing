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
import party.morino.moripafishing.api.model.world.LocationData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Command("mf world")
@Permission("moripa_fishing.command.world")
class WorldCommand: KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val worldManager : WorldManager by inject()

    @Command("transfer <world> <player>")
    @Permission("moripa_fishing.command.world.default")
    fun transfer(sender: CommandSender, @Argument("world") fishingWorld : FishingWorld,@Argument("player") player: Player) {
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

    @Command("list")
    @Permission("moripa_fishing.command.world.default")
    fun list(sender: CommandSender) {
        sender.sendMessage("Fishing Worlds:")
        worldManager.getWorldIdList().forEach { fishingWorld ->
            val worldDetails = worldManager.getWorld(fishingWorld).getWorldDetails()
            //TODO もっといい書き方
            sender.sendRichMessage("<green>${worldDetails.name}</green>")
        }
    }

    @Command("create <id> [generator] [biome]")
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

    @Command("delete <world>")
    @Permission("moripa_fishing.command.world.delete")
    suspend fun delete(sender: CommandSender, @Argument("world") world: FishingWorld) {
        if(!worldManager.getWorldIdList().contains(world.getId())) {
            sender.sendMessage("World ${world.getId()} does not exist.")
            return
        }
        val res = withContext(Dispatchers.minecraft){
            worldManager.deleteWorld(world.getId())
        }
        if(!res){
            sender.sendMessage("Failed to delete world ${world.getId()}.")
            return
        }
        sender.sendMessage("Deleted world ${world.getId()} (but folder is not deleted).")
    }

    // @Command("world reload <world>")
    // @Permission("moripa_fishing.command.world.reload")
    // suspend fun reload(sender: CommandSender, @Argument("world") world: FishingWorld) {
    //     //TODO ワールドの再読み込み
    //     world.reloadWorlds()
    //     sender.sendMessage("Reloaded all worlds.")
    // }


    @Command("config set spawn <world>")
    @Permission("moripa_fishing.command.world.config")
    suspend fun setSpawn(sender: CommandSender, @Argument("world") world: FishingWorld) {
        if(sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return
        }
        val location = sender.location
        val locationData = LocationData(location.x, location.y, location.z, location.yaw.toDouble(), location.pitch.toDouble())
        //TODO ワールドのスポーン位置の設定
        world.setWorldSpawnPosition(locationData)
        sender.sendMessage("Spawn position set for world ${world.getId()}.")
    }

    @Command("config set center <world> <x> <z>")
    @Permission("moripa_fishing.command.world.config")
    suspend fun setCenter(sender: CommandSender, @Argument("world") world: FishingWorld, @Argument("x") x: Double, @Argument("z") z: Double) {
        world.setCenter(x, z)
        sender.sendMessage("Center set for world ${world.getId()}.")
    }

    @Command("config set radius <world> <radius>")
    @Permission("moripa_fishing.command.world.config")
    suspend fun setRadius(sender: CommandSender, @Argument("world") world: FishingWorld, @Argument("radius") radius: Double) {
        world.setRadius(radius)
        sender.sendMessage("Radius set for world ${world.getId()}.")
    }

    @Command("config view <world>")
    @Permission("moripa_fishing.command.world.config")
    suspend fun view(sender: CommandSender, @Argument("world") world: FishingWorld) {
        val details = world.getWorldDetails()
        val text = Json{
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }.encodeToString(details)
        sender.sendMessage(text)
    }
    
    

    


}