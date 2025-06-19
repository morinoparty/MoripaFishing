package party.morino.moripafishing.ui.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.fishing.FishingManager
import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.api.model.rod.RodPresetId
import party.morino.moripafishing.utils.rod.RodAnalyzer
import java.util.Locale

@Command("mf rod")
@Permission("moripa_fishing.command.rod")
class RodCommand : KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val fishingManager: FishingManager by inject()
    private val rodPresetManager by lazy { fishingManager.getRodPresetManager() }
    private val rodAnalyzer by lazy { RodAnalyzer(plugin) }

    @Command("create <type> [multiplier] [addSeconds]")
    @Permission("moripa_fishing.command.rod.create")
    suspend fun createRod(
        sender: CommandSender,
        @Argument(value = "type", parserName = "rodType") rodType: String,
        @Argument("multiplier") multiplier: Double = 1.0,
        @Argument("addSeconds") addSeconds: Double = 0.0,
    ) {
        val player =
            sender as? Player ?: run {
                sender.sendRichMessage("<red>This command can only be used by players.")
                return
            }

        val bonusEffects = mutableListOf<ApplyValue>()

        // 倍率効果を追加
        if (multiplier != 1.0) {
            bonusEffects.add(ApplyValue(ApplyType.MULTIPLY, multiplier, "seconds"))
        }

        // 加算効果を追加
        if (addSeconds != 0.0) {
            bonusEffects.add(ApplyValue(ApplyType.ADD, addSeconds, "seconds"))
        }

        val rodConfig =
            RodConfiguration(
                rodType = RodPresetId(rodType),
                bonusEffects = bonusEffects,
                displayNameKey = "${rodType.uppercase()} ROD",
                loreKeys =
                    listOf(
                        "Wait Time Multiplier: ${(multiplier * 100).toInt()}%",
                        "Additional Time: ${addSeconds}s",
                        "Special Rod for MoripaFishing",
                    ),
            )

        val fishingRod = ItemStack(Material.FISHING_ROD)
        rodAnalyzer.setRodConfiguration(fishingRod, rodConfig, sender.locale())

        player.inventory.addItem(fishingRod)
        player.sendRichMessage("<green>Created <yellow>${rodConfig.displayNameKey}</yellow> rod!")
    }

    @Command("preset <presetName>")
    @Permission("moripa_fishing.command.rod.preset")
    suspend fun createPresetRod(
        sender: CommandSender,
        @Argument(value = "presetName", parserName = "rodPreset") rodConfig: RodConfiguration,
    ) {
        val player =
            sender as? Player ?: run {
                sender.sendRichMessage("<red>This command can only be used by players.")
                return
            }

        val fishingRod = ItemStack(Material.FISHING_ROD)
        rodAnalyzer.setRodConfiguration(fishingRod, rodConfig, sender.locale())

        player.inventory.addItem(fishingRod)
        player.sendRichMessage("<green>Created preset rod: <yellow><lang:${rodConfig.displayNameKey}></yellow>!")
    }

    @Command("info")
    @Permission("moripa_fishing.command.rod.info")
    suspend fun rodInfo(sender: CommandSender) {
        val player =
            sender as? Player ?: run {
                sender.sendRichMessage("<red>This command can only be used by players.")
                return
            }

        val heldItem = player.inventory.itemInMainHand
        if (heldItem.type != Material.FISHING_ROD) {
            player.sendRichMessage("<red>Please hold a fishing rod to view its information.")
            return
        }

        val customRodConfig = rodAnalyzer.analyzeRod(heldItem)
        val vanillaRodConfig = rodAnalyzer.analyzeVanillaRod(heldItem)

        when {
            customRodConfig != null -> {
                player.sendRichMessage("<green>Custom Rod Information:")
                player.sendRichMessage("<yellow>Type: <white>${customRodConfig.rodType}")
                player.sendRichMessage("<yellow>Wait Time Multiplier: <white>${customRodConfig.waitTimeMultiplier}")
                player.sendRichMessage("<yellow>Bonus Effects: <white>${customRodConfig.bonusEffects.size}")
                player.sendRichMessage("<yellow>Weather Immunity: <white>${customRodConfig.weatherImmunity}")
                if (customRodConfig.fishingWorldBonuses.isNotEmpty()) {
                    player.sendRichMessage("<yellow>Fishing World Bonuses:")
                    customRodConfig.fishingWorldBonuses.forEach { (worldId, bonus) ->
                        player.sendRichMessage("  <gray>$worldId: <white>${bonus}x")
                    }
                }
            }

            vanillaRodConfig != null -> {
                player.sendRichMessage("<green>Enchanted Vanilla Rod:")
                player.sendRichMessage("<yellow>Bonus Effects: <white>${vanillaRodConfig.bonusEffects.size}")
                vanillaRodConfig.bonusEffects.forEach { effect ->
                    player.sendRichMessage("  <gray>${effect.type}: <white>${effect.value} ${effect.unit}")
                }
            }

            else -> {
                player.sendRichMessage("<yellow>Standard vanilla fishing rod with no special effects.")
            }
        }
    }

    @Command("list")
    @Permission("moripa_fishing.command.rod.list")
    suspend fun listPresets(sender: CommandSender) {
        val presets = rodPresetManager.getAllPresetIds()

        sender.sendRichMessage("<green>Available Rod Presets:")
        presets.forEach { presetId ->
            val config = rodPresetManager.getPreset(presetId)
            if (config != null) {
                val translatedName =
                    if (config.displayNameKey.isNotEmpty()) {
                        GlobalTranslator.render(
                            Component.translatable(config.displayNameKey),
                            Locale.getDefault(),
                        )
                    } else {
                        Component.text(presetId.value)
                    }
                sender.sendRichMessage("<yellow>${presetId.value} <gray>- ${MiniMessage.miniMessage().serialize(translatedName)}")
            }
        }
        sender.sendRichMessage("")
        sender.sendRichMessage("<gray>Use '/mf rod preset <name>' to create preset rods")
        sender.sendRichMessage("<gray>Use '/mf rod create <type> [multiplier] [addSeconds]' for custom rods")
    }
}
