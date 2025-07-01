package party.morino.moripafishing.api.core.random.fish

import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.model.rarity.RarityId

/**
 * 魚の選択プロバイダーを管理するマネージャーインターフェース
 * 複数のプロバイダーの中から適切なものを選択して魚の抽選を行う
 */
interface FishSelectionManager {
    /**
     * 魚選択プロバイダーを登録する
     *
     * @param provider 登録するプロバイダー
     */
    fun registerProvider(provider: FishSelectionProvider)

    /**
     * 魚選択プロバイダーの登録を解除する
     *
     * @param provider 登録解除するプロバイダー
     */
    fun unregisterProvider(provider: FishSelectionProvider)

    /**
     * 登録されているプロバイダーの一覧を取得する
     *
     * @return プロバイダーのリスト（優先度順）
     */
    fun getProviders(): List<FishSelectionProvider>

    /**
     * 指定されたコンテキストで魚を選択する
     * 登録されたプロバイダーを優先度順に評価し、
     * 最初に処理可能なプロバイダーで魚を選択する
     *
     * @param context 魚の選択に関するコンテキスト情報
     * @return 選択された魚
     */
    fun selectFish(context: FishSelectionContext): Fish

    /**
     * 指定されたコンテキストでレアリティを選択する
     * 登録されたプロバイダーを優先度順に評価し、
     * カスタムレアリティ選択がある場合はそれを使用する
     *
     * @param context 魚の選択に関するコンテキスト情報
     * @return 選択されたレアリティ
     */
    fun selectRarity(context: FishSelectionContext): RarityId

    /**
     * すべてのプロバイダーの登録を解除する
     */
    fun clearProviders()
}
