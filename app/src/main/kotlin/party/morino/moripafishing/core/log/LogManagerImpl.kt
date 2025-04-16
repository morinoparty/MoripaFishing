package party.morino.moripafishing.core.log

import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.core.log.LogManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing

/**
 * LogManagerの実装クラス
 */
class LogManagerImpl : LogManager, KoinComponent {
    val plugin : MoripaFishing by inject()
    /**
     * 魚の情報を標準出力にログとして出す
     * @param fish ログに出力する魚の情報
     */
    override fun logFish(fish: Fish) {
        // 魚の情報を標準出力に出力する
        plugin.logger.info("Caught fish: ${fish.getId().value}, Length: ${fish.getSize()}, Worth: ${fish.getWorth()}")
        // TODO: 将来的にはファイルやデータベースへのロギングを検討する
    }
} 