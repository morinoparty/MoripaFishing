package party.morino.moripafishing.mocks.log

import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.core.log.LogManager

/**
 * LogManagerのモッククラスなのだ
 */
class LogManagerMock : LogManager {
    /**
     * 魚の情報をログに出力するのだ（モック用、何もしない）
     * @param fish ログに出力する魚の情報
     */
    override fun logFish(fish: Fish) {
        println("Caught fish: ${fish.getId().value}, Length: ${fish.getSize()}, Worth: ${fish.getWorth()}")
        // モックなので実際には何もしないのだ
    }

    /**
     * INFOレベルのログを記録する（モック用）
     */
    override fun info(message: String) {
        println("INFO: $message")
    }

    /**
     * WARNINGレベルのログを記録する（モック用）
     */
    override fun warning(message: String) {
        println("WARNING: $message")
    }

    /**
     * SEVEREレベルのログを記録する（モック用）
     */
    override fun severe(message: String) {
        println("SEVERE: $message")
    }
}
