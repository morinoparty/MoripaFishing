package party.morino.moripafishing.utils.commands

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.incendo.cloud.SenderMapper

class CommandSenderMapper : SenderMapper<CommandSourceStack, CommandSender> {
    override fun map(source: CommandSourceStack): CommandSender = source.sender

    override fun reverse(sender: CommandSender): CommandSourceStack {
        return object : CommandSourceStack {
            override fun getLocation(): Location {
                if (sender is Entity) {
                    return sender.location
                }
                val worlds = Bukkit.getWorlds()
                return Location(if (worlds.isEmpty()) null else worlds.first(), 0.0, 0.0, 0.0)
            }

            override fun getSender(): CommandSender = sender

            override fun getExecutor(): Entity? = sender as? Entity

            override fun getPlayerOrThrow(): Player = executor as? Player ?: throw PLAYER_REQUIRED.create()

            override fun getEntityOrThrow(): Entity = executor ?: throw ENTITY_REQUIRED.create()

            override fun withLocation(location: Location): CommandSourceStack = sender as CommandSourceStack

            override fun withExecutor(executor: Entity): CommandSourceStack = sender as CommandSourceStack
        }
    }

    companion object {
        private val PLAYER_REQUIRED = SimpleCommandExceptionType(LiteralMessage("A player is required to run this command here"))
        private val ENTITY_REQUIRED = SimpleCommandExceptionType(LiteralMessage("An entity is required to run this command here"))
    }
}
