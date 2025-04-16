package party.morino.moripafishing.mocks.log

import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.core.log.LogManager

/**
 * LogManagerのモッククラスなのだ
 */
class LogManagerMock : LogManager {
    val loggedFish = mutableListOf<Fish>() // ログに出力された魚を記録するリスト

    /**
     * 魚の情報をログに出力するのだ（モック用、何もしない）
     * @param fish ログに出力する魚の情報
     */
    override fun logFish(fish: Fish) {
        loggedFish.add(fish) // 記録だけしておくのだ
        // モックなので実際には何もしないのだ
    }
} 