package party.morino.moripafishing.addons.chum.listener

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import party.morino.moripafishing.addons.chum.ChumPermissions
import party.morino.moripafishing.addons.chum.config.ChumConfig
import party.morino.moripafishing.addons.chum.config.ChumDefinition
import party.morino.moripafishing.addons.chum.config.ChumScope
import party.morino.moripafishing.addons.chum.effect.ChumActivation
import party.morino.moripafishing.addons.chum.effect.ChumEffectStore
import party.morino.moripafishing.addons.chum.effect.ChumTarget
import party.morino.moripafishing.addons.chum.item.ChumItems
import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.model.angler.AnglerId
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.UUID

/**
 * メインハンドの撒き餌アイテムを右クリックで使い、効果を有効にする。
 *
 * 範囲 ([ChumScope.AREA]) とワールド ([ChumScope.WORLD]) の撒き餌は、釣りワールドの中でだけ使える。
 * 使えなかった場合はアイテムを消費しない。
 *
 * 右クリックを押し続けるとイベントが繰り返し発火するため、使った直後の [USE_COOLDOWN] の間は同じプレイヤーによる同じ撒き餌の使用を無視する。
 *
 * @property api 釣りワールドの判定に使う
 * @property store 効果の保存先
 * @property items 撒き餌アイテムの判定
 * @property config 現在の設定
 * @property clock 連続使用の判定に使う時計
 */
class ChumUseListener(
    private val api: MoripaFishingAPI,
    private val store: ChumEffectStore,
    private val items: ChumItems,
    private val config: () -> ChumConfig,
    private val clock: Clock,
) : Listener {
    private val lastUse = HashMap<Pair<UUID, String>, Instant>()

    // 空気への右クリックは最初から isCancelled が true のため、ignoreCancelled ではなくアイテムの使用可否で判定する
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND || !event.action.isRightClick) return
        if (event.useItemInHand() == Event.Result.DENY) return
        val chumId = items.chumId(event.item) ?: return
        // 撒き餌のブロックを置いたり食べたりしないよう、バニラの動作は常に止める
        event.isCancelled = true
        val player = event.player
        val last = lastUse[player.uniqueId to chumId]
        if (last != null && clock.instant().isBefore(last.plus(USE_COOLDOWN))) return
        val chum = config().chums[chumId]
        when {
            !player.hasPermission(ChumPermissions.USE) -> player.sendRichMessage("<red>撒き餌を使う権限がありません。")
            chum == null -> player.sendRichMessage("<red>この撒き餌は現在使えません。")
            else -> use(player, chumId, chum)
        }
    }

    private fun use(
        player: Player,
        chumId: String,
        chum: ChumDefinition,
    ) {
        val anglerId = AnglerId(player.uniqueId)
        val location = api.getAnglerManager().getAngler(anglerId).getLocation()
        val target =
            when (chum.scope) {
                ChumScope.ANGLER -> ChumTarget.Angler(anglerId)
                ChumScope.AREA -> location?.let { ChumTarget.Area(it.worldId, it.x, it.z, checkNotNull(chum.radius)) }
                ChumScope.WORLD -> location?.let { ChumTarget.World(it.worldId) }
            }
        if (target == null) {
            player.sendRichMessage("<red>この撒き餌は釣りワールドでのみ使えます。")
            return
        }
        val remaining =
            player.inventory.itemInMainHand
                .clone()
                .apply { amount -= 1 }
        player.inventory.setItemInMainHand(remaining.takeIf { it.amount > 0 })
        val now = clock.instant()
        // 冷却時間を過ぎた記録は不要なので、記録を追加する前に捨てて増え続けないようにする
        lastUse.values.removeIf { !now.isBefore(it.plus(USE_COOLDOWN)) }
        lastUse[player.uniqueId to chumId] = now
        val activation = store.activate(chumId, target, Duration.ofSeconds(chum.durationSeconds), chum.charges)
        player.sendRichMessage(
            message(chum, activation),
            Placeholder.component("chum", MiniMessage.miniMessage().deserialize(chum.item.name)),
            Placeholder.unparsed("duration", chum.durationSeconds.toString()),
            Placeholder.unparsed("radius", String.format(Locale.ROOT, "%.1f", chum.radius ?: 0.0)),
            Placeholder.unparsed("charges", chum.charges?.toString().orEmpty()),
        )
    }

    private fun message(
        chum: ChumDefinition,
        activation: ChumActivation,
    ): String {
        val head = if (activation.refreshed) "<chum><green> の効果を更新しました。" else "<chum><green> を使いました。"
        val scope =
            when (chum.scope) {
                ChumScope.ANGLER -> "あなたの釣り"
                ChumScope.AREA -> "半径 <radius> ブロック以内の釣り"
                ChumScope.WORLD -> "このワールドの釣り"
            }
        val charges = if (chum.charges != null) "（釣果 <charges> 回まで）" else ""
        return "$head<gray>${scope}に <duration> 秒間効果があります$charges。"
    }

    private companion object {
        /** 使った直後に同じプレイヤーによる同じ撒き餌の使用を無視する時間 */
        val USE_COOLDOWN: Duration = Duration.ofSeconds(1)
    }
}
