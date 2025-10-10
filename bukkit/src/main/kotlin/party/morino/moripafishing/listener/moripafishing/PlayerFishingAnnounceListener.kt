package party.morino.moripafishing.listener.moripafishing

import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import party.morino.moripafishing.event.fishing.AnglerFishCaughtEvent

class PlayerFishingAnnounceListener :
    Listener,
    KoinComponent {
    @EventHandler
    fun onPlayerFishingAnnounce(event: AnglerFishCaughtEvent) {
        val angler = event.getAngler().getName()
        val caughtFish = event.getCaughtFish()
        val fishTranslateKey = caughtFish.fish.toTranslateKey()

        val translateTags =
            listOf(
                Argument.component("fish_name", Component.translatable(fishTranslateKey)),
                Argument.component("size", Component.text(String.format("%.2f", caughtFish.size))),
                Argument.component("angler", Component.text(angler)),
                Argument.component(
                    "world",
                    Component.translatable("moripa_fishing.world.${caughtFish.world.value}.name"),
                ),
                Argument.component("timestamp", Component.text(caughtFish.timestamp.toString())),
            )

        val audience: ForwardingAudience = Bukkit.getServer()
        audience.sendMessage(
            Component.translatable(
                "moripa_fishing.message.angler_fish_caught",
                translateTags,
            ),
        )
    }
}
