package party.morino.moripafishing.addons.chum.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player
import party.morino.moripafishing.addons.chum.ChumPermissions

/** `/chum give` で一度に渡せる個数の上限 (プレイヤーのインベントリ 36 枠 × 64 個) */
private const val MAX_GIVE_AMOUNT = 36 * 64

/**
 * `/chum` コマンドのツリーを作る。処理は [commands] に委ねる。
 *
 * @param commands 各サブコマンドの処理
 */
fun chumCommand(commands: ChumCommands): LiteralCommandNode<CommandSourceStack> =
    Commands
        .literal("chum")
        .then(
            Commands
                .literal("give")
                .requires { it.sender.hasPermission(ChumPermissions.GIVE) }
                .then(
                    Commands
                        .argument("player", ArgumentTypes.player())
                        .then(
                            Commands
                                .argument("chum", StringArgumentType.word())
                                .suggests { _, builder ->
                                    commands
                                        .chumIds()
                                        .filter { it.startsWith(builder.remainingLowerCase) }
                                        .forEach(builder::suggest)
                                    builder.buildFuture()
                                }.executes { give(commands, it, 1) }
                                .then(
                                    Commands
                                        .argument("amount", IntegerArgumentType.integer(1, MAX_GIVE_AMOUNT))
                                        .executes { give(commands, it, IntegerArgumentType.getInteger(it, "amount")) },
                                ),
                        ),
                ),
        ).then(
            Commands
                .literal("odds")
                .requires { it.sender.hasPermission(ChumPermissions.ODDS) }
                .executes { context ->
                    val self = context.source.executor as? Player
                    if (self == null) {
                        context.source.sender.sendRichMessage("<red>プレイヤーを指定してください。")
                    } else {
                        commands.odds(context.source.sender, self)
                    }
                    Command.SINGLE_SUCCESS
                }.then(
                    Commands
                        .argument("player", ArgumentTypes.player())
                        .requires { it.sender.hasPermission(ChumPermissions.ODDS_OTHERS) }
                        .executes { context ->
                            commands.odds(context.source.sender, player(context))
                            Command.SINGLE_SUCCESS
                        },
                ),
        ).then(
            Commands
                .literal("reload")
                .requires { it.sender.hasPermission(ChumPermissions.RELOAD) }
                .executes { context ->
                    commands.reload(context.source.sender)
                    Command.SINGLE_SUCCESS
                },
        ).build()

private fun give(
    commands: ChumCommands,
    context: CommandContext<CommandSourceStack>,
    amount: Int,
): Int {
    commands.give(context.source.sender, player(context), StringArgumentType.getString(context, "chum"), amount)
    return Command.SINGLE_SUCCESS
}

private fun player(context: CommandContext<CommandSourceStack>): Player =
    context
        .getArgument("player", PlayerSelectorArgumentResolver::class.java)
        .resolve(context.source)
        .first()
