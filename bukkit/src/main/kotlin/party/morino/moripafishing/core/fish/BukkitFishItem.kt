import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.translation.Argument
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.getKoin
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.model.fish.CaughtFish
import java.util.Locale

/**
 * BukkitのItemStackを利用した魚アイテムの実装クラス
 *
 * このクラスは、魚のデータをBukkitのItemStackとして表現し、Minecraft内でアイテムとして扱えるようにする
 */
class BukkitFishItem : KoinComponent {
    /**
     * 魚アイテムを作成する
     * @param caughtFish 捕獲された魚のデータ
     * @return ItemStack 魚アイテム
     */
    companion object {
        /**
         * 魚からItemStackを作成する
         * @param fish 魚
         * @return ItemStack
         */
        fun create(
            caughtFish: CaughtFish,
            locale: Locale,
        ): ItemStack {
            // Koinから依存関係を取得
            val fishManager = getKoin().get<FishManager>()
            val configManager = getKoin().get<ConfigManager>()
            val anglerManager = getKoin().get<AnglerManager>()
            val plugin = getKoin().get<MoripaFishing>()

            // 魚の基本データを取得
            val fishData = fishManager.getFishWithId(caughtFish.fish) ?: throw IllegalArgumentException("Fish not found")

            // マテリアルからItemStackを作成
            val item =
                ItemStack(
                    Material.getMaterial(fishData.itemStack.material)
                        ?: throw IllegalArgumentException("Material not found"),
                )

            // カスタムモデルデータのコンポーネントを取得
            val customModelDataComponent = item.itemMeta.getCustomModelDataComponent()

            // 永続化データ用のキーを作成
            val key = NamespacedKey(plugin, "moripa_fishing.fish")

            val anglerName =
                anglerManager.getAnglerByAnglerUniqueId(caughtFish.angler)?.getName()
                    ?: throw IllegalArgumentException("Angler not found")

            // 翻訳用のタグを整理して作成
            val globalTranslator = GlobalTranslator.translator()

            // 各種情報をComponent化してArgumentリストにまとめる
            val translateTags =
                listOf(
                    Argument.component(
                        "rarity",
                        globalTranslator.translate(
                            Component.translatable("moripa_fishing.rarity.${fishData.rarity.value}.name"),
                            locale,
                        ) ?: Component.text(fishData.rarity.value),
                    ),
                    Argument.component(
                        "size",
                        Component.text("%.2f".format(caughtFish.size)),
                    ),
                    Argument.component(
                        "angler",
                        Component.text(anglerName),
                    ),
                    Argument.component(
                        "world",
                        globalTranslator.translate(
                            Component
                                .translatable("moripa_fishing.world.${caughtFish.world.value}.name"),
                            locale,
                        ) ?: Component.text(caughtFish.world.value),
                    ),
                    Argument.component(
                        "timestamp",
                        Component.text(caughtFish.timestamp.toString()),
                    ),
                )

            // 魚の説明文（lore）をComponentリストとして生成
            val defaultLoreKeys =
                listOf(
                    "moripa_fishing.fish.lore.default.rarity",
                    "moripa_fishing.fish.lore.default.size",
                    "moripa_fishing.fish.lore.default.angler",
                )

            // デフォルトlore部分をComponent化
            val defaultLoreComponents =
                defaultLoreKeys.map {
                    Component.translatable(it, translateTags)
                }

            // 追加lore部分をComponent化
            val additionalLoreComponents =
                fishData.lore[configManager.getConfig().defaultLocale]
                    ?.mapIndexed { index, _ ->
                        Component.translatable(
                            "moripa_fishing.fish.lore.${caughtFish.fish.value}.additional.$index",
                            translateTags,
                        )
                    } ?: emptyList()

            // 全てのloreをまとめて翻訳し、イタリック装飾を外す
            val translatableComponents: List<Component> =
                (defaultLoreComponents + additionalLoreComponents)
                    .mapNotNull { globalTranslator.translate(it, locale) }
                    .map { it.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) }

            // カスタムモデルデータを設定
            customModelDataComponent.floats = fishData.itemStack.itemMeta.customModelData

            // ItemMetaを更新
            item.itemMeta =
                item.itemMeta.apply {
                    displayName(globalTranslator.translate(Component.translatable("moripa_fishing.fish.${fishData.id.value}.name"), locale))
                    lore(translatableComponents)
                    // 永続化データコンテナに魚のデータを保存
                    persistentDataContainer.set(key, PersistentDataType.STRING, caughtFish.uniqueId.toString())
                    setCustomModelDataComponent(customModelDataComponent)
                }
            return item
        }

        /**
         * アイテムが魚アイテムかどうかを判定する
         * @param item 判定するアイテム
         * @return 魚アイテムの場合はtrue
         */
        fun isBukkitFishItem(item: ItemStack): Boolean {
            // Koinからプラグインインスタンスを取得
            val plugin = getKoin().get<MoripaFishing>()

            // 永続化データ用のキーを作成
            val key = NamespacedKey(plugin, "moripa_fishing.fish")

            // 永続化データコンテナを取得
            val persistentDataContainer = item.itemMeta.persistentDataContainer

            // キーが存在するかどうかを確認
            return persistentDataContainer.has(key, PersistentDataType.STRING)
        }
    }
}
