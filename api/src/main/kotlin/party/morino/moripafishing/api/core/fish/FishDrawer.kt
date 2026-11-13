package party.morino.moripafishing.api.core.fish

import party.morino.moripafishing.api.model.fish.Fish
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.world.FishingWorldId

/**
 * 魚を抽選する。任意のスレッドから呼べる。
 */
interface FishDrawer {
    /**
     * 釣りワールドの現在の天候と魚の条件から、抽選の候補を作る。
     *
     * 返した [FishDrawPool] の重みを変更してから [draw] に渡すと、変更後の重みで抽選する。
     *
     * @param world 釣りワールドの ID
     * @return 抽選の候補。未登録の釣りワールドでは天候が不明として扱われる
     */
    fun createPool(world: FishingWorldId): FishDrawPool

    /**
     * 候補の現在の重みに従い、レアリティ・魚を抽選してサイズと価値を決める。
     *
     * @param pool 抽選の候補
     * @return 抽選された魚。抽選対象が無ければ `null`
     */
    fun draw(pool: FishDrawPool): Fish?

    /**
     * 指定した魚のサイズと価値のみ抽選する。
     *
     * @param data 魚の定義
     * @throws IllegalArgumentException 魚のレアリティが未登録の場合
     */
    fun roll(data: FishData): Fish
}
