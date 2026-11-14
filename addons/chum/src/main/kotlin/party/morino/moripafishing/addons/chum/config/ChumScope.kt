package party.morino.moripafishing.addons.chum.config

import kotlinx.serialization.Serializable

/**
 * 撒き餌の効果が及ぶ範囲。
 */
@Serializable
enum class ChumScope {
    /** 使用したつりびと本人。[ChumDefinition.charges] で釣果の回数を制限できる */
    ANGLER,

    /** 使用した位置から水平距離 [ChumDefinition.radius] ブロック以内の釣り針 */
    AREA,

    /** 使用した釣りワールド全体 */
    WORLD,
}
