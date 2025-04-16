package party.morino.moripafishing.api.core.log

import party.morino.moripafishing.api.core.fish.Fish

/**
 * ログを管理するインターフェースなのだ
 */
interface LogManager {
    /**
     * 魚の情報をログに出力するのだ
     * @param fish ログに出力する魚の情報
     */
    fun logFish(fish: Fish)
} 