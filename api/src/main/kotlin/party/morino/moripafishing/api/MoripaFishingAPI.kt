package party.morino.moripafishing.api

import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishDrawer
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager

/**
 * MoripaFishing の公開 API。[MoripaFishingAPIProvider] から取得する。
 */
interface MoripaFishingAPI {
    /**
     * 設定 (`config.json`) の読み取り口を返す。
     */
    fun getConfigManager(): ConfigManager

    /**
     * 魚の定義を管理するマネージャーを返す。
     */
    fun getFishManager(): FishManager

    /**
     * レアリティの定義を管理するマネージャーを返す。
     */
    fun getRarityManager(): RarityManager

    /**
     * 魚の抽選を行う [FishDrawer] を返す。
     */
    fun getFishDrawer(): FishDrawer

    /**
     * 釣りワールドを管理するマネージャーを返す。
     */
    fun getWorldManager(): WorldManager

    /**
     * 釣り人を取得するマネージャーを返す。
     */
    fun getAnglerManager(): AnglerManager
}
