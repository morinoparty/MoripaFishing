package party.morino.moripafishing.mocks.internationalization

import party.morino.moripafishing.api.core.internationalization.TranslateManager

class TranslateManagerMock : TranslateManager {
    override fun load() {}
    override fun reload() {}
    override fun loadWorldData() {}
    override fun loadRarityData() {}
    override fun loadFishData() {}
}
