package party.morino.moripafishing.config

import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.ConfigData
import party.morino.moripafishing.api.config.WeatherConfig

/**
 * ConfigManagerのモッククラス
 * テスト用の設定値を提供する
 */
class ConfigManagerMock : ConfigManager {
    override fun reload() {
        // モックでは何もしない
    }

    override fun getConfig(): ConfigData {
        return ConfigData(
            weather = WeatherConfig(
                dayCycleTimeZone = "Asia/Tokyo",
                interval = 8,
                offset = 0,
                frequency = 0.15
            )
        )
    }
} 