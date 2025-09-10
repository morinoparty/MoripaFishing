package party.morino.moripafishing.core.internationalization

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore
import net.kyori.adventure.translation.GlobalTranslator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.PluginDirectory
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import java.util.Locale
import java.util.Properties

object TranslateManager : KoinComponent {
    private val pluginDirectory: PluginDirectory by inject()
    private val configManager: ConfigManager by inject()
    private val fishManager: FishManager by inject()
    private val rarityManager: RarityManager by inject()
    private val worldManager: WorldManager by inject()

    lateinit var myStore: MiniMessageTranslationStore

    fun load() {
        myStore = MiniMessageTranslationStore.create(Key.key("moripafishing:translations"))
        myStore.defaultLocale(configManager.getConfig().defaultLocale)

        loadFromResources()

        loadFishData()
        loadWorldData()
        GlobalTranslator.translator().addSource(myStore)
    }

    private fun loadFromResources() {
        val locales = listOf(Locale.JAPAN, Locale.US)
        locales.forEach { locale ->
            val resourcePath = "/translate/$locale.properties"
            val filePath = pluginDirectory.getTranslationDirectory().resolve("$locale.properties")
            if (!filePath.exists()) {
                // リソースからコピー UTF-8で保存されていることを期待
                this::class.java.getResourceAsStream(resourcePath)?.use { input ->
                    filePath.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            // ファイルから読み込み UTF-8で保存されていることを期待
            val properties = Properties()
            filePath.inputStream().use { input ->
                properties.load(input.reader(Charsets.UTF_8))
            }
            properties.forEach { (key, value) ->
                myStore.register(key.toString(), locale, value.toString())
            }
        }
    }

    fun loadFishData() {
        fishManager.getFish().forEach { fish ->
            fish.lore.forEach { (locale, list) ->
                list.forEachIndexed { index, lore ->
                    myStore.register("moripa_fishing.fish.lore.${fish.id.value}.additional.$index", locale, lore)
                }
            }
            fish.displayName.forEach { (locale, name) ->
                myStore.register("moripa_fishing.fish.${fish.id.value}.name", locale, name)
            }
        }

        rarityManager.getRarities().forEach { rarity ->
            rarity.displayName.forEach { (locale, name) ->
                myStore.register("moripa_fishing.rarity.${rarity.id.value}.name", locale, name)
            }
        }
    }

    fun loadWorldData() {
        worldManager.getWorldIdList().forEach { worldId ->
            val world = worldManager.getWorld(worldId)
            world.getWorldDetails().name.forEach { (locale, name) ->
                myStore.register("moripa_fishing.world.${worldId.value}.name", locale, name)
            }
        }
    }
}
