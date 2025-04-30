import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.getKoin
import party.morino.moripafishing.api.core.fish.CaughtFish
import party.morino.moripafishing.api.core.fish.FishManager
import java.util.Locale
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import party.morino.moripafishing.MoripaFishing

/**
 * BukkitのItemStackを利用した魚アイテムの実装クラス
 */
class BukkitFishItem : KoinComponent {
    companion object {
        /**
         * 魚からItemStackを作成する
         * @param fish 魚
         * @return ItemStack
         */
        fun create(caughtFish: CaughtFish): ItemStack {
            val fishManager = getKoin().get<FishManager>()
            val plugin = getKoin().get<MoripaFishing>()
            val fishData = fishManager.getFishWithId(caughtFish.getId()) ?: throw IllegalArgumentException("Fish not found")
            val item =
                ItemStack(
                    Material.getMaterial(fishData.itemStack.material)
                        ?: throw IllegalArgumentException("Material not found"),
                )
            val customModelDataComponent = item.itemMeta.getCustomModelDataComponent()
            // TODO Add to persistant data container
            val key = NamespacedKey(plugin, "moripa_fishing.fish")
            val persistentDataContainer = item.itemMeta.persistentDataContainer

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
            val translatableComponents: List<Component> =
                arrayListOf(
                    Component.translatable("moripa_fishing.fish.lore.rarity", translateTags),
                    Component.translatable("moripa_fishing.fish.lore.size", translateTags),
                    Component.translatable("moripa_fishing.fish.lore.angler", translateTags),
                ) +
                    fishData.lore.get(Locale.getDefault())!!.mapIndexed { index, _ ->
                        Component.translatable("moripa_fishing.fish.${caughtFish.getId().value}.lore.additional.$index", translateTags)
                    }.toList()
            customModelDataComponent.floats = fishData.itemStack.itemMeta.customModelData
            item.itemMeta =
                item.itemMeta.apply {
                    displayName(Component.text("moripa_fishing.fish.${fishData.id.value}.name"))
                    lore(translatableComponents)
                    persistentDataContainer.set(key, PersistentDataType.STRING, caughtFish.getId().value)
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
            val plugin = getKoin().get<MoripaFishing>()
            val key = NamespacedKey(plugin, "moripa_fishing.fish")
            val persistentDataContainer = item.itemMeta.persistentDataContainer
            return persistentDataContainer.has(key, PersistentDataType.STRING)
        }
    }
}
