package party.morino.moripafishing.core.log

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.log.LogManager

/**
 * LogManagerの実装クラス
 */
class LogManagerImpl :
    LogManager,
    KoinComponent {
    private val plugin: MoripaFishing by inject()

    /**
     * INFOレベルのログを出力する
     */
    override fun info(message: String) {
        plugin.logger.info(message)
    }

    /**
     * WARNINGレベルのログを出力する
     */
    override fun warning(message: String) {
        plugin.logger.warning(message)
    }

    /**
     * SEVEREレベルのログを出力する
     */
    override fun severe(message: String) {
        plugin.logger.severe(message)
    }
}
