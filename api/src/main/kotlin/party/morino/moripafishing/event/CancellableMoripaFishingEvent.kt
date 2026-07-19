package party.morino.moripafishing.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event

/**
 * キャンセル可能な MoripaFishing イベントの共通基底クラス。
 *
 * `isCancelled` / `setCancelled` の定型実装を提供する。
 * Bukkit の仕様上、`HandlerList` と静的な `getHandlerList()` は各イベントクラスが個別に持つ必要がある。
 */
abstract class CancellableMoripaFishingEvent(
    async: Boolean = false,
) : Event(async),
    Cancellable {
    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }
}
