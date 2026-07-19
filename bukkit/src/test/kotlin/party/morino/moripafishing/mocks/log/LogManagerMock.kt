package party.morino.moripafishing.mocks.log

import party.morino.moripafishing.api.core.log.LogManager

/**
 * LogManagerのモッククラス
 */
class LogManagerMock : LogManager {
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
