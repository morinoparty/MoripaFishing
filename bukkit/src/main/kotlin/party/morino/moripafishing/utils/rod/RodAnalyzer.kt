package party.morino.moripafishing.utils.rod

import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.fishing.EnchantmentEffects
import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.utils.Utils
import java.util.Locale

class RodAnalyzer(private val plugin: Plugin) : KoinComponent {
    private val namespace = NamespacedKey(plugin, "rod_waiting")

    fun analyzeRod(itemStack: ItemStack?): RodConfiguration? {
        if (itemStack?.type != Material.FISHING_ROD) return null

        val meta = itemStack.itemMeta ?: return null
        val nbtData = meta.persistentDataContainer.get(namespace, PersistentDataType.STRING)

        return try {
            nbtData?.let { Utils.json.decodeFromString<RodConfiguration>(it) }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to parse rod configuration from NBT: ${e.message}")
            null
        }
    }

    fun analyzeVanillaRod(itemStack: ItemStack?): RodConfiguration? {
        if (itemStack?.type != Material.FISHING_ROD) return null

        val meta = itemStack.itemMeta ?: return null
        val enchantments = meta.enchants
        val bonusEffects = mutableListOf<ApplyValue>()

        // 入れ食いエンチャント (Lure) - マインクラフト標準効果（レベルあたり5秒短縮）
        val lureLevel = enchantments[org.bukkit.enchantments.Enchantment.LURE] ?: 0
        if (lureLevel > 0) {
            EnchantmentEffects.getLureEffect(lureLevel)?.let { applyValue ->
                bonusEffects.add(applyValue)
            }
        }

        // 海运エンチャント (Luck of the Sea) - 本来は待ち時間に影響しないが軽微な効果を適用
        val luckLevel = enchantments[org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA] ?: 0
        if (luckLevel > 0) {
            EnchantmentEffects.getLuckOfTheSeaEffect(luckLevel)?.let { applyValue ->
                bonusEffects.add(applyValue)
            }
        }

        return if (bonusEffects.isNotEmpty()) {
            RodConfiguration(
                rodType = "vanilla_enchanted",
                waitTimeMultiplier = 1.0,
                bonusEffects = bonusEffects,
            )
        } else {
            null
        }
    }

    fun setRodConfiguration(
        itemStack: ItemStack,
        configuration: RodConfiguration,
        locale: Locale,
    ): ItemStack {
        if (itemStack.type != Material.FISHING_ROD) return itemStack

        val meta = itemStack.itemMeta ?: return itemStack
        val jsonString = Utils.json.encodeToString(RodConfiguration.serializer(), configuration)

        meta.persistentDataContainer.set(namespace, PersistentDataType.STRING, jsonString)

        // 表示名とLoreを設定（多言語対応）
        if (configuration.displayNameKey.isNotEmpty()) {
            val translatedName =
                GlobalTranslator.render(
                    Component.translatable(configuration.displayNameKey),
                    locale,
                )
            meta.displayName(translatedName)
        }

        if (configuration.loreKeys.isNotEmpty()) {
            val translatedLore =
                configuration.loreKeys.map { loreKey ->
                    GlobalTranslator.render(
                        Component.translatable(loreKey),
                        locale,
                    )
                }
            meta.lore(translatedLore)
        }

        itemStack.itemMeta = meta
        return itemStack
    }

    fun getAllEffects(rodConfig: RodConfiguration): List<ApplyValue> {
        val effects = mutableListOf<ApplyValue>()

        // 基本倍率効果
        if (rodConfig.waitTimeMultiplier != 1.0) {
            effects.add(ApplyValue(ApplyType.MULTIPLY, rodConfig.waitTimeMultiplier, "seconds"))
        }

        // 追加効果
        effects.addAll(rodConfig.bonusEffects)

        return effects
    }
}
