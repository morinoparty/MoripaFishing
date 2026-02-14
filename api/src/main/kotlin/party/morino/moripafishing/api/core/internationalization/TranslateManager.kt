package party.morino.moripafishing.api.core.internationalization

interface TranslateManager {
    fun load()
    fun reload()

    fun loadWorldData()
    fun loadRarityData()
    fun loadFishData()
}