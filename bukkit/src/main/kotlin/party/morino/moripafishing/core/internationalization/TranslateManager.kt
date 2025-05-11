package party.morino.moripafishing.core.internationalization

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore
import net.kyori.adventure.translation.GlobalTranslator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.world.WorldManager
import java.util.Locale

object TranslateManager : KoinComponent {
    private val fishManager: FishManager by inject()

    private val worldManager: WorldManager by inject()

    lateinit var myStore: MiniMessageTranslationStore

    fun load()  {
        myStore = MiniMessageTranslationStore.create(Key.key("moripafishing:translations"))

        loadFishData()
        loadWorldData()

        GlobalTranslator.translator().addSource(myStore)
    }

    fun loadFishData()  {
        myStore.register("moripa_fishing.fish.lore.default.rarity", Locale.JAPAN, "<gray>レアリティ: </gray> <rarity>")
        myStore.register("moripa_fishing.fish.lore.default.rarity", Locale.ENGLISH, "<gray>Rarity: </gray> <rarity>")

        myStore.register("moripa_fishing.fish.lore.default.weight", Locale.JAPAN, "<gray>重さ: </gray> <weight>")
        myStore.register("moripa_fishing.fish.lore.default.weight", Locale.ENGLISH, "<gray>Weight: </gray> <weight>")

        myStore.register("moripa_fishing.fish.lore.default.size", Locale.JAPAN, "<gray>サイズ: </gray> <size>")
        myStore.register("moripa_fishing.fish.lore.default.size", Locale.ENGLISH, "<gray>Size: </gray> <size>")

        fishManager.getFish().forEach { fish ->
            fish.lore.forEach { locale, list ->
                list.forEachIndexed { index, lore ->
                    myStore.register("moripa_fishing.fish.lore.${fish.id}.additional.$index", locale, lore)
                }
            }
            fish.displayName.forEach { locale, name ->
                myStore.register("moripa_fishing.fish.${fish.id}.name", locale, name)
            }
        }
    }

    fun loadWorldData()  {
        worldManager.getWorldIdList().forEach { worldId ->
            val world = worldManager.getWorld(worldId)
            myStore.register("moripa_fishing.world.${worldId.value}.name", Locale.JAPAN, world.getWorldDetails().name)
        }
    }
}
