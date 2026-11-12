package party.morino.moripafishing.api.core.fish

import party.morino.moripafishing.api.model.fish.Fish
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 魚を抽選する。任意のスレッドから呼べる。
 */
interface FishDrawer {
    /**
     * 釣りワールドの現在の天候と魚の条件に従い、レアリティ・魚・サイズ・価値を抽選する。
     *
     * @param world 釣りワールドの ID
     * @return 抽選された魚。候補が無ければ `null`
     */
    fun draw(world: FishingWorldId): Fish?

    /**
     * 指定した魚のサイズと価値のみ抽選する。
     *
     * @param data 魚の定義
     * @throws IllegalArgumentException 魚のレアリティが未登録の場合
     */
    fun roll(data: FishData): Fish
}
