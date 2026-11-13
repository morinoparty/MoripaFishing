package party.morino.moripafishing.addons.chum.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import party.morino.moripafishing.addons.chum.config.ChumConfig
import party.morino.moripafishing.addons.chum.effect.ActiveChum
import party.morino.moripafishing.addons.chum.effect.ChumEffectApplier
import party.morino.moripafishing.addons.chum.item.ChumItems
import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.model.angler.AnglerId
import java.time.Clock
import java.time.Duration
import java.util.Locale

/**
 * `/chum` の各サブコマンドの処理。コマンドの登録は [chumCommand] で行う。
 *
 * @property api 釣りワールドと抽選の候補の取得に使う
 * @property applier 効果の適用 (`/chum odds`)
 * @property items 撒き餌アイテムの作成 (`/chum give`)
 * @property config 現在の設定
 * @property clock 効果の残り時間の表示に使う時計
 * @property reload 設定を読み直す。不正な設定なら [IllegalArgumentException] を投げ、以前の設定を保つ
 */
class ChumCommands(
    private val api: MoripaFishingAPI,
    private val applier: ChumEffectApplier,
    private val items: ChumItems,
    private val config: () -> ChumConfig,
    private val clock: Clock,
    private val reload: () -> Unit,
) {
    /** 設定されている撒き餌の ID を返す。 */
    fun chumIds(): Set<String> = config().chums.keys

    /**
     * `/chum give <player> <chum> [amount]`: 撒き餌アイテムを渡す。インベントリに入りきらない分は足元に落とす。
     */
    fun give(
        sender: CommandSender,
        target: Player,
        chumId: String,
        amount: Int,
    ) {
        val chum = config().chums[chumId]
        if (chum == null) {
            sender.sendRichMessage("<red>撒き餌 <id> は設定されていません。", Placeholder.unparsed("id", chumId))
            return
        }
        // 最大スタック数を超えるスタックを作らないよう、分けてから渡す
        val item = items.create(chumId, chum, 1)
        val stacks =
            generateSequence(amount) { it - item.maxStackSize }
                .takeWhile { it > 0 }
                .map { remaining -> item.clone().apply { this.amount = minOf(remaining, item.maxStackSize) } }
                .toList()
        val overflow = target.inventory.addItem(*stacks.toTypedArray())
        overflow.values.forEach { target.world.dropItem(target.location, it) }
        sender.sendRichMessage(
            "<green><player> に <chum><green> を <amount> 個渡しました。",
            Placeholder.unparsed("player", target.name),
            Placeholder.component("chum", MiniMessage.miniMessage().deserialize(chum.item.name)),
            Placeholder.unparsed("amount", amount.toString()),
        )
    }

    /**
     * `/chum odds [player]`: プレイヤーの現在位置で、有効な撒き餌の効果を適用した後の魚ごとの確率を表示する。
     *
     * 確率 0 の候補 (餌限定の魚など) も含めて、確率の高い順に表示する。
     * 範囲の撒き餌は、釣り針の代わりにプレイヤーの位置で判定する。効果の残り回数は減らさない。
     */
    fun odds(
        sender: CommandSender,
        target: Player,
    ) {
        val anglerId = AnglerId(target.uniqueId)
        val location = api.getAnglerManager().getAngler(anglerId).getLocation()
        if (location == null) {
            sender.sendRichMessage("<red><player> は釣りワールドにいません。", Placeholder.unparsed("player", target.name))
            return
        }
        val pool = api.getFishDrawer().createPool(location.worldId)
        val effects = applier.apply(anglerId, location, pool)
        sender.sendRichMessage(
            "<gold><player> の釣れる確率</gold> <gray>(<world>)",
            Placeholder.unparsed("player", target.name),
            Placeholder.component("world", translatable(location.worldId.toTranslateKey(), location.worldId.value)),
        )
        sender.sendRichMessage("<gray>有効な撒き餌:")
        sendEffects(sender, effects)
        val odds =
            pool
                .getFishes()
                .map { it to pool.getProbability(it.id) }
                .sortedByDescending { it.second }
        if (odds.none { it.second > 0.0 }) {
            sender.sendRichMessage("<gray>抽選できる魚がいません (バニラのドロップになります)。")
            return
        }
        odds.forEach { (fish, probability) ->
            sender.sendRichMessage(
                "<white><percent>%</white> <fish> <gray>(<rarity>)",
                Placeholder.unparsed("percent", String.format(Locale.ROOT, "%.3f", probability * PERCENT)),
                Placeholder.component("fish", translatable(fish.id.toTranslateKey(), fish.id.value)),
                Placeholder.component("rarity", translatable(fish.rarity.toTranslateKey(), fish.rarity.value)),
            )
        }
    }

    /** `/chum reload`: `config.yml` を読み直す。有効な効果はそのまま残る。 */
    fun reload(sender: CommandSender) {
        try {
            reload()
            sender.sendRichMessage("<green>撒き餌の設定を読み直しました。")
        } catch (e: IllegalArgumentException) {
            // SerializationException を含む。不正な設定なら以前の設定のまま
            sender.sendRichMessage("<red>読み直しに失敗しました: <message>", Placeholder.unparsed("message", e.message.orEmpty()))
        }
    }

    private fun sendEffects(
        sender: CommandSender,
        effects: List<ActiveChum>,
    ) {
        if (effects.isEmpty()) {
            sender.sendRichMessage("<gray>  なし")
            return
        }
        val now = clock.instant()
        effects.forEach { effect ->
            val name = config().chums[effect.chumId]?.item?.name ?: effect.chumId
            val charges = effect.remainingCharges?.let { " / 残り $it 回" }.orEmpty()
            sender.sendRichMessage(
                "<gray>  <chum><gray> (残り <seconds> 秒<charges>)",
                Placeholder.component("chum", MiniMessage.miniMessage().deserialize(name)),
                Placeholder.unparsed("seconds", Duration.between(now, effect.expiresAt).toSeconds().toString()),
                Placeholder.unparsed("charges", charges),
            )
        }
    }

    private fun translatable(
        key: String,
        fallback: String,
    ): Component = Component.translatable(key, fallback)

    private companion object {
        const val PERCENT = 100.0
    }
}
