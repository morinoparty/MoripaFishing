package party.morino.moripafishing.testing

import party.morino.moripafishing.api.config.ConfigData
import party.morino.moripafishing.api.config.ConfigManager

/** 固定の [ConfigData] を返す [ConfigManager]。 */
internal class FakeConfigManager(
    var data: ConfigData = ConfigData(),
) : ConfigManager {
    override fun getConfig(): ConfigData = data
}
