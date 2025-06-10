package party.morino.moripafishing.core.log

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.core.log.LogManager

/**
 * LogManagerの実装クラス
 */
class LogManagerImpl : LogManager, KoinComponent {
    val plugin: MoripaFishing by inject()

    /**
     * 魚の情報を標準出力にログとして出す
     * @param fish ログに出力する魚の情報
     */
    override fun logFish(fish: Fish) {
        // 魚の情報を標準出力に出力する
        plugin.logger.info("Caught fish: ${fish.getId().value}, Length: ${fish.getSize()}, Worth: ${fish.getWorth()}")
        // TODO: 将来的にはファイルやデータベースへのロギングを検討する
    }

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
