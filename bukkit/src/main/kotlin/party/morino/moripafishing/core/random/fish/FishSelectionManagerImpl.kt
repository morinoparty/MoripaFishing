package party.morino.moripafishing.core.random.fish

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.core.random.fish.FishRandomizer
import party.morino.moripafishing.api.core.random.fish.FishSelectionContext
import party.morino.moripafishing.api.core.random.fish.FishSelectionManager
import party.morino.moripafishing.api.core.random.fish.FishSelectionProvider
import party.morino.moripafishing.api.model.rarity.RarityId
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 魚の選択プロバイダーを管理する実装クラス
 * 複数のプロバイダーの中から適切なものを選択して魚の抽選を行う
 */
class FishSelectionManagerImpl : FishSelectionManager, KoinComponent {
    // プロバイダーのリスト（スレッドセーフ）
    private val providers = CopyOnWriteArrayList<FishSelectionProvider>()

    // デフォルトの魚ランダマイザー
    private val defaultFishRandomizer: FishRandomizer by inject()

    /**
     * 魚選択プロバイダーを登録する
     *
     * @param provider 登録するプロバイダー
     */
    override fun registerProvider(provider: FishSelectionProvider) {
        providers.add(provider)
        // 優先度順でソート
        providers.sortByDescending { it.getPriority() }
    }

    /**
     * 魚選択プロバイダーの登録を解除する
     *
     * @param provider 登録解除するプロバイダー
     */
    override fun unregisterProvider(provider: FishSelectionProvider) {
        providers.remove(provider)
    }

    /**
     * 登録されているプロバイダーの一覧を取得する
     *
     * @return プロバイダーのリスト（優先度順）
     */
    override fun getProviders(): List<FishSelectionProvider> {
        return providers.toList()
    }

    /**
     * 指定されたコンテキストで魚を選択する
     * 登録されたプロバイダーを優先度順に評価し、
     * 最初に処理可能なプロバイダーで魚を選択する
     *
     * @param context 魚の選択に関するコンテキスト情報
     * @return 選択された魚
     */
    override fun selectFish(context: FishSelectionContext): Fish {
        // 登録されたプロバイダーを優先度順に評価
        for (provider in providers) {
            if (provider.canHandle(context)) {
                return provider.selectFish(context)
            }
        }

        // 該当するプロバイダーがない場合はデフォルトロジックを使用
        return defaultFishRandomizer.selectRandomFish(context.fishingWorld.getId())
    }

    /**
     * 指定されたコンテキストでレアリティを選択する
     * 登録されたプロバイダーを優先度順に評価し、
     * カスタムレアリティ選択がある場合はそれを使用する
     *
     * @param context 魚の選択に関するコンテキスト情報
     * @return 選択されたレアリティ
     */
    override fun selectRarity(context: FishSelectionContext): RarityId {
        // 登録されたプロバイダーを優先度順に評価
        for (provider in providers) {
            if (provider.canHandle(context)) {
                val customRarity = provider.selectRarity(context)
                if (customRarity != null) {
                    return customRarity
                }
            }
        }

        // カスタムレアリティ選択がない場合はデフォルトロジックを使用
        return defaultFishRandomizer.drawRandomRarity()
    }

    /**
     * すべてのプロバイダーの登録を解除する
     */
    override fun clearProviders() {
        providers.clear()
    }
}
