import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.getKoin
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.fish.CaughtFish
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.model.fish.CaughtFishData
import java.util.Locale

/**
 * BukkitのItemStackを利用した魚アイテムの実装クラス
 *
 * このクラスは、魚のデータをBukkitのItemStackとして表現し、Minecraft内でアイテムとして扱えるようにする
 */
class BukkitFishItem : KoinComponent {
    companion object {
        /**
         * 魚からItemStackを作成する
         * @param fish 魚
         * @return ItemStack
         */
        fun create(caughtFish: CaughtFish): ItemStack {
            // Koinから依存関係を取得
            val fishManager = getKoin().get<FishManager>()
            val plugin = getKoin().get<MoripaFishing>()

            // 魚の基本データを取得
            val fishData = fishManager.getFishWithId(caughtFish.getId()) ?: throw IllegalArgumentException("Fish not found")

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

            // 捕獲された魚のデータを取得
            val caughtFishData = CaughtFishData.from(caughtFish)

            // 翻訳用のタグを準備
            val translateTags =
                listOf(
                    Argument.component("rarity", Component.translatable("moripa_fishing.fish.${fishData.rarity.value}.name")),
                    Argument.component("size", Component.text(caughtFish.getSize().toString())),
                    Argument.component("angler", Component.text(caughtFish.getAngler().getName())),
                    Argument.component(
                        "world",
                        Component.translatable("moripa_fishing.world.${caughtFish.getCaughtAtWorld().getId().value}.name"),
                    ),
                    Argument.component("timestamp", Component.text(caughtFish.getCaughtAt().toString())),
                )

            // 翻訳可能なコンポーネントを準備
            val translatableComponents: List<Component> =
                arrayListOf(
                    Component.translatable("moripa_fishing.fish.lore.rarity", translateTags),
                    Component.translatable("moripa_fishing.fish.lore.size", translateTags),
                    Component.translatable("moripa_fishing.fish.lore.angler", translateTags),
                ) +
                    fishData.lore.get(Locale.getDefault())!!.mapIndexed { index, _ ->
                        Component.translatable("moripa_fishing.fish.${caughtFish.getId().value}.lore.additional.$index", translateTags)
                    }.toList()

            // カスタムモデルデータを設定
            customModelDataComponent.floats = fishData.itemStack.itemMeta.customModelData

            // ItemMetaを更新
            item.itemMeta =
                item.itemMeta.apply {
                    displayName(Component.text("moripa_fishing.fish.${fishData.id.value}.name"))
                    lore(translatableComponents)
                    // 永続化データコンテナに魚のデータを保存
                    persistentDataContainer.set(key, PersistentDataType.STRING, Utils.json.encodeToString(caughtFishData))
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
