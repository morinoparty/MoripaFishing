package party.morino.moripafishing.api.core.fish

import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.world.FishingWorld
import java.time.ZonedDateTime

/**
 * 魚を表すインターフェース
 */
interface CaughtFish : Fish {
    /**
     * 魚の捕獲日時を取得する
     * @return 魚の捕獲日時
     */
    fun getCaughtAt(): ZonedDateTime

    /**
     * 魚の捕獲場所を取得する
     * @return 魚の捕獲場所
     */
    fun getCaughtAtWorld(): FishingWorld

    /**
     * 魚の釣りびとを取得する
     * @return 魚の釣りびと
     */
    fun getAngler(): Angler
}
