package party.morino.moripafishing.ui.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

@Command("moripa_fishing")
@Permission("moripa_fishing.command.weather")
class WeatherCommand: KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val worldManager : WorldManager by inject()

    @Command("weather set <weather>")
    @Permission("moripa_fishing.command.weather")
    fun weather(sender : CommandSourceStack, weather: WeatherType){
        if(sender.sender !is Player) {
            sender.sender.sendRichMessage("<red> This command can only be used by players.")
            return
        }
        worldManager.getWorld(FishingWorldId("default")).setWeather(weather)
    }

    @Command("world default")
    @Permission("moripa_fishing.command.world.default")
    fun worldDefault(source: CommandSourceStack) {
        val sender = source.sender
        if(sender !is Player) {
            sender.sendRichMessage("<red> This command can only be used by players.")
            return
        }
        val namespacedKey = NamespacedKey(plugin, worldManager.getDefaultWorldId().value)
        val world = Bukkit.getWorld(namespacedKey)
        if (world != null) {
            sender.sendRichMessage("<green> The default world is ${world.name}.")
        } else {
            sender.sendRichMessage("<red> The default world does not exist.")
        }
        sender.teleportAsync(Location(world, 0.0, 100.0, 0.0))
    }
}