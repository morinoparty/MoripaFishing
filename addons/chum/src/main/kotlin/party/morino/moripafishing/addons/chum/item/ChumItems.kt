package party.morino.moripafishing.addons.chum.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import party.morino.moripafishing.addons.chum.config.ChumDefinition

/**
 * 撒き餌アイテムを作り、アイテムの PDC から撒き餌の ID を読む。
 *
 * @param key 撒き餌の ID を保存する PDC のキー
 */
class ChumItems(
    private val key: NamespacedKey,
) {
    /**
     * 撒き餌アイテムを作る。
     *
     * @param chumId 撒き餌の ID
     * @param chum 撒き餌の定義。Material は読み込み時に検証済みであること
     * @param amount 個数
     */
    fun create(
        chumId: String,
        chum: ChumDefinition,
        amount: Int,
    ): ItemStack {
        val item = ItemStack(checkNotNull(Material.matchMaterial(chum.item.material)), amount)
        item.editMeta { meta ->
            meta.displayName(parse(chum.item.name))
            if (chum.item.lore.isNotEmpty()) meta.lore(chum.item.lore.map(::parse))
            if (chum.item.customModelData.isNotEmpty()) {
                val component = meta.customModelDataComponent
                component.floats = chum.item.customModelData
                meta.setCustomModelDataComponent(component)
            }
            meta.persistentDataContainer.set(key, PersistentDataType.STRING, chumId)
        }
        return item
    }

    /**
     * 撒き餌アイテムなら撒き餌の ID を返す。それ以外は `null`。
     *
     * @param item 調べるアイテム
     */
    fun chumId(item: ItemStack?): String? {
        if (item == null || item.isEmpty) return null
        return item.persistentDataContainer.get(key, PersistentDataType.STRING)
    }

    private fun parse(text: String): Component =
        MiniMessage.miniMessage().deserialize(text).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
}
