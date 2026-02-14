package party.morino.moripafishing.ui.commands

import org.bukkit.command.CommandSender
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.internationalization.TranslateManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager

@Command("mf")
@Permission("moripa_fishing.command.world")
class DefaultCommand : KoinComponent {
    private val worldManager: WorldManager by inject()
    private val fishManager: FishManager by inject()
    private val rarityManager: RarityManager by inject()
    private val configManager: ConfigManager by inject()
    private val translateManager: TranslateManager by inject()

    @Command("reload")
    @Permission("moripa_fishing.command.world.default")
    fun transfer(sender: CommandSender) {
        val n = 5
        // config
        configManager.reload()
        sender.sendRichMessage("<green>[1 / $n] Reloaded root config")

        // world
        worldManager.getWorldIdList().forEach { worldId ->
            val fishingWorld = worldManager.getWorld(worldId)
            fishingWorld.loadConfig()
            fishingWorld.updateState()
        }
        sender.sendRichMessage("<green>[2 / $n] Reloaded world")

        // rarity
        rarityManager.unloadRarities()
        rarityManager.loadRarities()
        sender.sendRichMessage("<green>[3 / $n] Reloaded all rarities")

        // fishing
        fishManager.unloadFishes()
        fishManager.loadFishes()
        sender.sendRichMessage("<green>[4 / $n] Reloaded all fishes")

        // i18n
        translateManager.reload()
        sender.sendRichMessage("<green>[5 / $n] Reloaded all i18n files")
    }
}
