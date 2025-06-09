package party.morino.moripafishing.utils.rod

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.model.rod.RodConfiguration

class RodAnalyzer(private val plugin: Plugin) : KoinComponent {
    private val configManager: ConfigManager by inject()
    
    private val namespace = NamespacedKey(plugin, "moripafishing")
    private val json = Json { 
        ignoreUnknownKeys = true
    }

    fun analyzeRod(itemStack: ItemStack?): RodConfiguration? {
        if (itemStack?.type != Material.FISHING_ROD) return null
        
        val meta = itemStack.itemMeta ?: return null
        val nbtData = meta.persistentDataContainer.get(namespace, PersistentDataType.STRING)
        
        return try {
            nbtData?.let { json.decodeFromString<RodConfiguration>(it) }
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
        
        val enchantmentConfig = configManager.getConfig().fishing.enchantmentEffects
        
        // 入れ食いエンチャント (Lure)
        val lureLevel = enchantments[org.bukkit.enchantments.Enchantment.LURE] ?: 0
        if (lureLevel > 0) {
            enchantmentConfig.lure[lureLevel]?.let { lureEffect ->
                bonusEffects.add(ApplyValue(ApplyType.MULTIPLY, lureEffect.timeMultiplier, "seconds"))
            }
        }
        
        // 海运エンチャント (Luck of the Sea)
        val luckLevel = enchantments[org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA] ?: 0
        if (luckLevel > 0) {
            enchantmentConfig.luckOfTheSea[luckLevel]?.let { luckEffect ->
                bonusEffects.add(ApplyValue(ApplyType.ADD, luckEffect.addSeconds, "seconds"))
            }
        }
        
        return if (bonusEffects.isNotEmpty()) {
            RodConfiguration(
                rodType = "vanilla_enchanted",
                waitTimeMultiplier = 1.0,
                bonusEffects = bonusEffects
            )
        } else {
            null
        }
    }
    
    fun setRodConfiguration(itemStack: ItemStack, configuration: RodConfiguration): ItemStack {
        if (itemStack.type != Material.FISHING_ROD) return itemStack
        
        val meta = itemStack.itemMeta ?: return itemStack
        val jsonString = json.encodeToString(RodConfiguration.serializer(), configuration)
        
        meta.persistentDataContainer.set(namespace, PersistentDataType.STRING, jsonString)
        
        // 表示名とLoreを設定（MiniMessage対応）
        if (configuration.displayName.isNotEmpty()) {
            meta.displayName(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize("<reset>${configuration.displayName}"))
        }
        
        if (configuration.lore.isNotEmpty()) {
            val miniMessage = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
            meta.lore(configuration.lore.map { miniMessage.deserialize("<gray>$it") })
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