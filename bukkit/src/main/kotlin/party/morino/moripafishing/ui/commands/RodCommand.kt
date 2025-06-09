package party.morino.moripafishing.ui.commands

import net.kyori.adventure.text.Component
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
import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.utils.rod.RodAnalyzer

@Command("mf rod")
@Permission("moripa_fishing.command.rod")
class RodCommand : KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val rodAnalyzer = RodAnalyzer(plugin)

    @Command("create <type> [multiplier] [addSeconds]")
    @Permission("moripa_fishing.command.rod.create")
    suspend fun createRod(
        sender: CommandSender,
        @Argument("type") rodType: String,
        @Argument("multiplier") multiplier: Double = 1.0,
        @Argument("addSeconds") addSeconds: Double = 0.0
    ) {
        val player = sender as? Player ?: run {
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

        val rodConfig = RodConfiguration(
            rodType = rodType,
            bonusEffects = bonusEffects,
            displayName = "${rodType.uppercase()} ROD",
            lore = listOf(
                "Wait Time Multiplier: ${(multiplier * 100).toInt()}%",
                "Additional Time: ${addSeconds}s",
                "Special Rod for MoripaFishing"
            )
        )

        val fishingRod = ItemStack(Material.FISHING_ROD)
        rodAnalyzer.setRodConfiguration(fishingRod, rodConfig)
        
        player.inventory.addItem(fishingRod)
        player.sendRichMessage("<green>Created <yellow>${rodConfig.displayName}</yellow> rod!")
    }

    @Command("preset <presetName>")
    @Permission("moripa_fishing.command.rod.preset")
    suspend fun createPresetRod(
        sender: CommandSender,
        @Argument("presetName") presetName: String
    ) {
        val player = sender as? Player ?: run {
            sender.sendRichMessage("<red>This command can only be used by players.")
            return
        }

        val rodConfig = when (presetName.lowercase()) {
            "beginner" -> RodConfiguration(
                rodType = "beginner",
                waitTimeMultiplier = 1.2,
                displayName = "Beginner's Rod",
                lore = listOf("Perfect for new anglers!", "Slightly slower fishing")
            )
            
            "master" -> RodConfiguration(
                rodType = "master",
                waitTimeMultiplier = 0.6,
                bonusEffects = listOf(ApplyValue(ApplyType.ADD, -3.0, "seconds")),
                weatherImmunity = true,
                displayName = "Master Angler's Rod",
                lore = listOf("For experienced fishermen", "Weather immunity", "Fast fishing speed")
            )
            
            "legendary" -> RodConfiguration(
                rodType = "legendary",
                waitTimeMultiplier = 0.4,
                bonusEffects = listOf(ApplyValue(ApplyType.CONSTANT, 2.0, "seconds")),
                biomeBonuses = mapOf(
                    "OCEAN" to 0.3,
                    "DEEP_OCEAN" to 0.2,
                    "RIVER" to 0.5
                ),
                displayName = "Legendary Fishing Rod",
                lore = listOf("Legendary power!", "Fixed 2s fishing time", "Biome bonuses")
            )
            
            "speedster" -> RodConfiguration(
                rodType = "speedster",
                waitTimeMultiplier = 0.3,
                bonusEffects = listOf(ApplyValue(ApplyType.ADD, -5.0, "seconds")),
                displayName = "Speedster Rod",
                lore = listOf("Ultra-fast fishing", "For speed fishing competitions")
            )
            
            else -> {
                player.sendRichMessage("<red>Unknown preset: $presetName")
                player.sendRichMessage("<yellow>Available presets: beginner, master, legendary, speedster")
                return
            }
        }

        val fishingRod = ItemStack(Material.FISHING_ROD)
        rodAnalyzer.setRodConfiguration(fishingRod, rodConfig)
        
        player.inventory.addItem(fishingRod)
        player.sendRichMessage("<green>Created preset rod: <yellow>${rodConfig.displayName}</yellow>!")
    }

    @Command("info")
    @Permission("moripa_fishing.command.rod.info")
    suspend fun rodInfo(sender: CommandSender) {
        val player = sender as? Player ?: run {
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
                if (customRodConfig.biomeBonuses.isNotEmpty()) {
                    player.sendRichMessage("<yellow>Biome Bonuses:")
                    customRodConfig.biomeBonuses.forEach { (biome, bonus) ->
                        player.sendRichMessage("  <gray>$biome: <white>${bonus}x")
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
        sender.sendRichMessage("<green>Available Rod Presets:")
        sender.sendRichMessage("<yellow>beginner <gray>- Slower rod for new players")
        sender.sendRichMessage("<yellow>master <gray>- Fast rod with weather immunity")
        sender.sendRichMessage("<yellow>legendary <gray>- Ultra-fast with biome bonuses")
        sender.sendRichMessage("<yellow>speedster <gray>- Fastest rod available")
        sender.sendRichMessage("")
        sender.sendRichMessage("<gray>Use '/mf rod preset <name>' to create preset rods")
        sender.sendRichMessage("<gray>Use '/mf rod create <type> [multiplier] [addSeconds]' for custom rods")
    }
}