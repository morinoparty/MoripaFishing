package party.morino.moripafishing.core.internationalization

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore
import net.kyori.adventure.translation.GlobalTranslator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.world.WorldManager
import java.util.Locale

object TranslateManager : KoinComponent {
    private val configManager: ConfigManager by inject()
    private val fishManager: FishManager by inject()

    private val worldManager: WorldManager by inject()

    lateinit var myStore: MiniMessageTranslationStore

    fun load() {
        myStore = MiniMessageTranslationStore.create(Key.key("moripafishing:translations"))
        myStore.defaultLocale(configManager.getConfig().defaultLocale)

        loadFishData()
        loadWorldData()
        loadMessageData()
        loadRodData()
        GlobalTranslator.translator().addSource(myStore)
    }

    fun loadFishData() {
        myStore.register("moripa_fishing.fish.lore.default.rarity", Locale.JAPAN, "<gray>レアリティ: </gray> <rarity>")
        myStore.register("moripa_fishing.fish.lore.default.rarity", Locale.ENGLISH, "<gray>Rarity: </gray> <rarity>")

        myStore.register("moripa_fishing.fish.lore.default.weight", Locale.JAPAN, "<gray>重さ: </gray> <weight>")
        myStore.register("moripa_fishing.fish.lore.default.weight", Locale.ENGLISH, "<gray>Weight: </gray> <weight>")

        myStore.register("moripa_fishing.fish.lore.default.size", Locale.JAPAN, "<gray>サイズ: </gray> <size>")
        myStore.register("moripa_fishing.fish.lore.default.size", Locale.ENGLISH, "<gray>Size: </gray> <size>")

        fishManager.getFish().forEach { fish ->
            fish.lore.forEach { locale, list ->
                list.forEachIndexed { index, lore ->
                    myStore.register("moripa_fishing.fish.lore.${fish.id.value}.additional.$index", locale, lore)
                }
            }
            fish.displayName.forEach { locale, name ->
                myStore.register("moripa_fishing.fish.${fish.id.value}.name", locale, name)
//                plugin.logger.info("Register fish name: moripa_fishing.fish.${fish.id.value}.name in ${locale} as ${name}")
            }
        }
    }

    fun loadWorldData() {
        worldManager.getWorldIdList().forEach { worldId ->
            val world = worldManager.getWorld(worldId)
            myStore.register("moripa_fishing.world.${worldId.value}.name", Locale.JAPAN, world.getWorldDetails().name)
        }
    }

    /**
     * メッセージデータを読み込む
     */
    fun loadMessageData() {
        // <fish_name>: 釣れた魚の名前
        // <size>: 釣れた魚のサイズ
        // <angler>: 釣り人の名前
        // <world>: 釣りが行われた世界の名前
        // <timestamp>: 釣りが行われた時間
        myStore.register(
            "moripa_fishing.message.angler_fish_caught",
            Locale.JAPAN,
            "🎣 <yellow>つり人: <angler> </yellow>が<world>で<size>cmの<fish_name>を釣りました。",
        )
        myStore.register(
            "moripa_fishing.message.angler_fish_caught",
            Locale.ENGLISH,
            "🎣 <yellow>Angler: <angler> </yellow>has caught <size>cm <fish_name> in <world>.",
        )
    }

    /**
     * ロッドデータを読み込む
     */
    fun loadRodData() {
        // Beginner Rod
        myStore.register("rod.beginner.name", Locale.JAPAN, "初心者の釣竿")
        myStore.register("rod.beginner.name", Locale.ENGLISH, "Beginner's Rod")
        myStore.register("rod.beginner.lore.1", Locale.JAPAN, "新人釣り師にぴったり！")
        myStore.register("rod.beginner.lore.1", Locale.ENGLISH, "Perfect for new anglers!")
        myStore.register("rod.beginner.lore.2", Locale.JAPAN, "少し釣りが遅くなります")
        myStore.register("rod.beginner.lore.2", Locale.ENGLISH, "Slightly slower fishing")

        // Master Rod
        myStore.register("rod.master.name", Locale.JAPAN, "マスター釣り師の竿")
        myStore.register("rod.master.name", Locale.ENGLISH, "Master Angler's Rod")
        myStore.register("rod.master.lore.1", Locale.JAPAN, "熟練釣り師のために")
        myStore.register("rod.master.lore.1", Locale.ENGLISH, "For experienced fishermen")
        myStore.register("rod.master.lore.2", Locale.JAPAN, "天候無効")
        myStore.register("rod.master.lore.2", Locale.ENGLISH, "Weather immunity")
        myStore.register("rod.master.lore.3", Locale.JAPAN, "高速釣り")
        myStore.register("rod.master.lore.3", Locale.ENGLISH, "Fast fishing speed")

        // Legendary Rod
        myStore.register("rod.legendary.name", Locale.JAPAN, "伝説の釣竿")
        myStore.register("rod.legendary.name", Locale.ENGLISH, "Legendary Fishing Rod")
        myStore.register("rod.legendary.lore.1", Locale.JAPAN, "伝説の力！")
        myStore.register("rod.legendary.lore.1", Locale.ENGLISH, "Legendary power!")
        myStore.register("rod.legendary.lore.2", Locale.JAPAN, "固定2秒釣り時間")
        myStore.register("rod.legendary.lore.2", Locale.ENGLISH, "Fixed 2s fishing time")
        myStore.register("rod.legendary.lore.3", Locale.JAPAN, "究極の釣り体験")
        myStore.register("rod.legendary.lore.3", Locale.ENGLISH, "Ultimate fishing experience")

        // Speedster Rod
        myStore.register("rod.speedster.name", Locale.JAPAN, "スピードスター竿")
        myStore.register("rod.speedster.name", Locale.ENGLISH, "Speedster Rod")
        myStore.register("rod.speedster.lore.1", Locale.JAPAN, "超高速釣り")
        myStore.register("rod.speedster.lore.1", Locale.ENGLISH, "Ultra-fast fishing")
        myStore.register("rod.speedster.lore.2", Locale.JAPAN, "スピード釣りコンペ用")
        myStore.register("rod.speedster.lore.2", Locale.ENGLISH, "For speed fishing competitions")
    }
}
