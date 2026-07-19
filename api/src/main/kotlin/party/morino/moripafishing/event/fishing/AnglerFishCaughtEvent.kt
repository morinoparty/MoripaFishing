package party.morino.moripafishing.event.fishing

import org.bukkit.event.HandlerList
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.model.fish.CaughtFish
import party.morino.moripafishing.event.CancellableMoripaFishingEvent

/**
 * このイベントは、釣り人が魚を釣ったときに発生します。
 *
 * キャンセルするとバニラの釣り上げごと取り消されます。
 * [setCaughtFish] で釣果を差し替えることができ、差し替えた内容がアイテム化と
 * 後続の [AnglerFishCaughtResultEvent] に反映されます。
 *
 * @param angler 釣りをした釣り人
 * @param caughtFish 釣れた魚（リスナーから変更可能）
 */
class AnglerFishCaughtEvent(
    private val angler: Angler,
    private var caughtFish: CaughtFish,
) : CancellableMoripaFishingEvent() {
    companion object {
        @JvmStatic
        private val HANDLER_LIST: HandlerList = HandlerList()

        /**
         * イベントのハンドラリストを取得します。
         * 必須メソッドです。
         * @return イベントのハンドラリスト
         */
        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    fun getAngler(): Angler = angler

    fun getCaughtFish(): CaughtFish = caughtFish

    /**
     * 釣果を差し替えます。
     * @param caughtFish 新しい釣果
     */
    fun setCaughtFish(caughtFish: CaughtFish) {
        this.caughtFish = caughtFish
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
