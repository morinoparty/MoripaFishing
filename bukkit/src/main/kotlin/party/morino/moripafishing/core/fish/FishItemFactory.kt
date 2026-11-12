package party.morino.moripafishing.core.fish

import com.destroystokyo.paper.profile.ProfileProperty
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.translation.Argument
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.api.model.fish.FishData
import java.util.Locale
import java.util.UUID

/**
 * 釣果から魚アイテムを作る。
 *
 * アイテムの PDC (`moripafishing:caught_fish`) には釣果の JSON を保存する。
 *
 * @property plugin PDC のキーとプロフィールの作成に使う
 * @property config 説明文の既定ロケールの参照先
 */
internal class FishItemFactory(
    private val plugin: Plugin,
    private val config: ConfigManager,
) {
    private val caughtFishKey = NamespacedKey(plugin, "caught_fish")

    /**
     * 魚アイテムを作る。
     *
     * @param fish 釣果
     * @param data 釣果の魚の定義
     * @param anglerName 釣り人の名前
     * @param locale 表示名と説明文のロケール
     */
    fun create(
        fish: CaughtFish,
        data: FishData,
        anglerName: String,
        locale: Locale,
    ): ItemStack {
        // Material は読み込み時に検証済み
        val item = ItemStack(checkNotNull(Material.matchMaterial(data.item.material)))
        val placeholders =
            mapOf(
                "rarity" to GlobalTranslator.render(Component.translatable(fish.rarity.toTranslateKey()), locale),
                "size" to Component.text(String.format(Locale.ROOT, "%.2f", fish.size)),
                "angler" to Component.text(anglerName),
                "world" to GlobalTranslator.render(Component.translatable(fish.world.toTranslateKey()), locale),
                "timestamp" to Component.text(fish.timestamp.toString()),
            )
        item.editMeta { meta ->
            meta.displayName(GlobalTranslator.render(Component.translatable(fish.fish.toTranslateKey()), locale).withoutItalic())
            meta.lore(lore(data, placeholders, locale))
            if (data.item.customModelData.isNotEmpty()) {
                val component = meta.customModelDataComponent
                component.floats = data.item.customModelData
                meta.setCustomModelDataComponent(component)
            }
            val texture = data.item.skullTexture
            if (texture != null && meta is SkullMeta) {
                meta.playerProfile =
                    plugin.server.createProfile(UUID.nameUUIDFromBytes(texture.toByteArray())).apply {
                        setProperty(ProfileProperty("textures", texture))
                    }
            }
            meta.persistentDataContainer.set(
                caughtFishKey,
                PersistentDataType.STRING,
                Json.encodeToString(CaughtFish.serializer(), fish),
            )
        }
        return item
    }

    /**
     * 既定の 3 行 (レアリティ・サイズ・釣り人) に、魚の説明文を続ける。
     *
     * 説明文は [locale]、既定ロケールの順に、それぞれ言語のみのロケールにも戻りながら探す。
     */
    private fun lore(
        data: FishData,
        placeholders: Map<String, Component>,
        locale: Locale,
    ): List<Component> {
        val arguments = DEFAULT_LORE_ARGUMENTS.map { Argument.component(it, placeholders.getValue(it)) }
        val defaults =
            DEFAULT_LORE_ARGUMENTS.map {
                GlobalTranslator.render(Component.translatable("moripa_fishing.fish.lore.default.$it", arguments), locale)
            }
        val resolver = TagResolver.resolver(placeholders.map { (name, value) -> Placeholder.component(name, value) })
        val lines = data.lore.lookup(locale) ?: data.lore.lookup(config.getConfig().defaultLocale) ?: emptyList()
        return (defaults + lines.map { MiniMessage.miniMessage().deserialize(it, resolver) }).map { it.withoutItalic() }
    }

    /** 翻訳ストアと同じく、完全一致のロケール、言語のみのロケールの順に探す。 */
    private fun <T> Map<Locale, T>.lookup(locale: Locale): T? = this[locale] ?: this[Locale.of(locale.language)]

    private fun Component.withoutItalic(): Component = decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)

    private companion object {
        /** 既定の説明文の行。翻訳キー `moripa_fishing.fish.lore.default.<name>` と同名の引数を使う。 */
        val DEFAULT_LORE_ARGUMENTS = listOf("rarity", "size", "angler")
    }
}
