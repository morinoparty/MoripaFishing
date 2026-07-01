package party.morino.moripafishing.addons.catchannounce

import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.event.fishing.AnglerFishCaughtEvent

/**
 * [AnglerFishCaughtEvent] を購読し、設定に応じて釣果を通知する。
 *
 * コア本体 (`MoripaFishing`) の [MoripaFishingAPI] はイベントごとに都度取得する。
 * こうすることで、コアの再読み込みなどでプラグインインスタンスが差し替わった場合にも追従できる。
 */
class CatchAnnounceListener(
    private val config: CatchAnnounceConfig,
) : Listener {
    private val miniMessage = MiniMessage.miniMessage()

    @EventHandler
    fun onAnglerFishCaught(event: AnglerFishCaughtEvent) {
        if (!config.enabled) return

        val caughtFish = event.getCaughtFish()
        val minRarityWeight = config.minRarityWeight
        if (minRarityWeight != null && caughtFish.rarity.toRarityData().weight > minRarityWeight) {
            return
        }

        val message = buildMessage(event, caughtFish)
        broadcast(message, caughtFish)
    }

    private fun buildMessage(
        event: AnglerFishCaughtEvent,
        caughtFish: CaughtFish,
    ): Component {
        val tagResolver =
            TagResolver.resolver(
                Placeholder.component("angler", Component.text(event.getAngler().getName())),
                Placeholder.component("fish_name", Component.translatable(caughtFish.fish.toTranslateKey())),
                Placeholder.component(
                    "rarity_name",
                    Component.translatable("moripa_fishing.rarity.${caughtFish.rarity.value}.name"),
                ),
                Placeholder.component("size", Component.text(String.format("%.2f", caughtFish.size))),
                Placeholder.component(
                    "world_name",
                    Component.translatable("moripa_fishing.world.${caughtFish.world.value}.name"),
                ),
                Placeholder.component("timestamp", Component.text(caughtFish.timestamp.toString())),
            )
        return miniMessage.deserialize(config.messageFormat, tagResolver)
    }

    private fun broadcast(
        message: Component,
        caughtFish: CaughtFish,
    ) {
        when (config.broadcastTarget) {
            BroadcastTarget.ALL -> {
                val audience: ForwardingAudience = Bukkit.getServer()
                audience.sendMessage(message)
            }
            BroadcastTarget.SAME_WORLD -> broadcastToSameWorld(message, caughtFish)
            BroadcastTarget.PERMISSION -> {
                Bukkit
                    .getOnlinePlayers()
                    .filter { player -> player.hasPermission(config.broadcastPermission) }
                    .forEach { player -> player.sendMessage(message) }
            }
        }
    }

    private fun broadcastToSameWorld(
        message: Component,
        caughtFish: CaughtFish,
    ) {
        val api = Bukkit.getPluginManager().getPlugin("MoripaFishing") as? MoripaFishingAPI ?: return
        api
            .getAnglerManager()
            .getOnlineAnglers()
            .filter { angler -> angler.getWorld()?.getId() == caughtFish.world }
            .mapNotNull { angler -> Bukkit.getPlayer(angler.getMinecraftUniqueId()) as Player? }
            .forEach { player -> player.sendMessage(message) }
    }
}
