package party.morino.moripafishing.api.core.random.fish

import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.model.rarity.RarityId

/**
 * 魚の選択をカスタマイズするためのプロバイダーインターフェース
 * プラグイン開発者は、このインターフェースを実装することで
 * 独自の魚の抽選ロジックを提供できる
 */
interface FishSelectionProvider {
    /**
     * プロバイダーの優先度を返す
     * より高い優先度のプロバイダーが先に評価される
     *
     * @return 優先度（0以上の整数）
     */
    fun getPriority(): Int = 0

    /**
     * このプロバイダーが指定された条件で魚の選択を行うかどうかを判定する
     *
     * @param context 魚の選択に関するコンテキスト情報
     * @return true: このプロバイダーで選択を行う, false: 他のプロバイダーまたはデフォルトロジックに委譲
     */
    fun canHandle(context: FishSelectionContext): Boolean

    /**
     * 魚を選択する
     * canHandle()がtrueを返した場合にのみ呼び出される
     *
     * @param context 魚の選択に関するコンテキスト情報
     * @return 選択された魚
     */
    fun selectFish(context: FishSelectionContext): Fish

    /**
     * レアリティの選択をカスタマイズする
     * nullを返した場合はデフォルトロジックが使用される
     *
     * @param context 魚の選択に関するコンテキスト情報
     * @return 選択されたレアリティ、またはnull（デフォルトロジック使用）
     */
    fun selectRarity(context: FishSelectionContext): RarityId? = null
}
