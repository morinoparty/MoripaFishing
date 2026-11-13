package party.morino.moripafishing.api.core.world

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 定義済み (設定ファイルが存在する) 釣りワールドの読み取り専用ビュー。
 *
 * 対応する Bukkit ワールドが未ロードでも存在する。ロード済みかどうかは
 * `Bukkit.getWorld(getWorldKey())` で判定する。
 * 設定の変更は `WorldManager.updateWorldConfig` で行う。
 */
interface FishingWorld {
    /**
     * ワールド ID を返す。
     */
    fun getId(): FishingWorldId

    /**
     * 対応する Bukkit ワールドのキーを返す。
     * 管理ワールドは `moripafishing:<id>`、外部ワールドは `WorldBinding.External.key`。
     */
    fun getWorldKey(): Key

    /**
     * 現在の設定 (不変スナップショット) を返す。任意のスレッドから呼べる。
     */
    fun getConfig(): FishingWorldConfig

    /**
     * 魚の抽選条件に使う現在の天候を返す。任意のスレッドから呼べる。
     */
    fun getWeather(): WeatherType
}
