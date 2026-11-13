package party.morino.moripafishing.addons.chum.listener

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import party.morino.moripafishing.addons.chum.config.ChumConfig
import party.morino.moripafishing.addons.chum.effect.ChumEffectApplier
import party.morino.moripafishing.addons.chum.effect.ChumEffectStore
import party.morino.moripafishing.api.event.fishing.AnglerFishCaughtResultEvent
import party.morino.moripafishing.api.event.fishing.AnglerFishDrawEvent
import party.morino.moripafishing.api.model.angler.AnglerId
import java.util.UUID

/**
 * 抽選の前に撒き餌の効果を適用し、釣果が確定したら回数の制限がある効果の残り回数を減らす。
 *
 * 残り回数は、その抽選に実際に適用した効果だけを減らす。
 * 釣果がキャンセルされた場合やバニラのドロップのままの場合は [AnglerFishCaughtResultEvent] が発火しないため、減らさない。
 *
 * @property applier 効果の適用
 * @property store 残り回数を減らす効果の保存先
 * @property config 現在の設定 (効果が切れたときのメッセージに使う)
 */
class ChumDrawListener(
    private val applier: ChumEffectApplier,
    private val store: ChumEffectStore,
    private val config: () -> ChumConfig,
) : Listener {
    // 抽選から釣果の確定までは同じ PlayerFishEvent の中で同期的に進むため、つりびとごとに直前の抽選の分だけを持つ
    private val pendingCharges = HashMap<AnglerId, List<UUID>>()

    @EventHandler
    fun onDraw(event: AnglerFishDrawEvent) {
        val angler = event.getAngler().getId()
        val applied = applier.apply(angler, event.getHookLocation(), event.getPool())
        val charged = applied.filter { it.remainingCharges != null }.map { it.id }
        if (charged.isEmpty()) pendingCharges.remove(angler) else pendingCharges[angler] = charged
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onCaught(event: AnglerFishCaughtResultEvent) {
        val angler = event.getAngler()
        val charged = pendingCharges.remove(angler.getId()) ?: return
        charged
            .mapNotNull(store::consumeCharge)
            .filter { it.remainingCharges == 0 }
            .forEach { effect ->
                val name = config().chums[effect.chumId]?.item?.name ?: effect.chumId
                val chum = Placeholder.component("chum", MiniMessage.miniMessage().deserialize(name))
                angler.getAudience().sendMessage(MiniMessage.miniMessage().deserialize("<chum><gray> の効果が切れました。", chum))
            }
    }
}
