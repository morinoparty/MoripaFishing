package party.morino.moripafishing.addons.catchannounce

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.event.fishing.AnglerFishCaughtResultEvent
import party.morino.moripafishing.api.model.fish.CaughtFish
import java.util.Locale

/**
 * [AnglerFishCaughtResultEvent] を購読し、設定に応じて釣果を通知する。
 *
 * @property api MoripaFishing の API
 * @property server 通知先の取得元
 * @property config Addon の設定
 */
class CatchAnnounceListener(
    private val api: MoripaFishingAPI,
    private val server: Server,
    private val config: CatchAnnounceConfig,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onAnglerFishCaught(event: AnglerFishCaughtResultEvent) {
        val caughtFish = event.getCaughtFish()
        val maxRarityWeight = config.maxRarityWeight
        if (maxRarityWeight != null) {
            val rarityWeight = api.getRarityManager().get(caughtFish.rarity)?.weight ?: return
            if (rarityWeight > maxRarityWeight) return
        }
        broadcast(buildMessage(event.getAngler().getName(), caughtFish), caughtFish)
    }

    private fun buildMessage(
        anglerName: String,
        caughtFish: CaughtFish,
    ): Component {
        val tagResolver =
            TagResolver.resolver(
                Placeholder.component("angler", Component.text(anglerName)),
                Placeholder.component("fish_name", Component.translatable(caughtFish.fish.toTranslateKey())),
                Placeholder.component("rarity_name", Component.translatable(caughtFish.rarity.toTranslateKey())),
                Placeholder.component("size", Component.text(String.format(Locale.ROOT, "%.2f", caughtFish.size))),
                Placeholder.component("world_name", Component.translatable(caughtFish.world.toTranslateKey())),
                Placeholder.component("timestamp", Component.text(caughtFish.timestamp.toString())),
            )
        return MiniMessage.miniMessage().deserialize(config.messageFormat, tagResolver)
    }

    private fun broadcast(
        message: Component,
        caughtFish: CaughtFish,
    ) {
        when (config.broadcastTarget) {
            // Server 自体の Audience と同じく、全プレイヤーとコンソールに送る
            BroadcastTarget.ALL -> Audience.audience(server.onlinePlayers + server.consoleSender).sendMessage(message)
            BroadcastTarget.SAME_WORLD ->
                api
                    .getAnglerManager()
                    .getOnlineAnglers()
                    .filter { it.getWorld()?.getId() == caughtFish.world }
                    .forEach { it.getAudience().sendMessage(message) }
            BroadcastTarget.PERMISSION ->
                server.onlinePlayers
                    .filter { it.hasPermission(config.broadcastPermission) }
                    .forEach { it.sendMessage(message) }
        }
    }
}
