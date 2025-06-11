package party.morino.moripafishing.core.random.fish

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.random.fish.FishProbabilityManager
import party.morino.moripafishing.api.core.random.fish.FishRandomizer
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.fish.FishData
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.core.fish.FishBuilderImpl
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

/**
 * 釣りシステムにおける魚の抽選を実装するクラス
 * レアリティや釣り場に応じた魚の抽選ロジックを提供する
 */
class FishRandomizerImpl : FishRandomizer, KoinComponent {
    // 乱数生成器
    private val random = Random()

    // 魚の管理を行うインスタンス
    private val fishManager: FishManager by inject()

    // レアリティの管理を行うインスタンス
    private val rarityManager: RarityManager by inject()

    // ワールドの管理を行うインスタンス
    private val worldManager: WorldManager by inject()

    // 確率修正値を管理するインスタンス
    private val fishProbabilityManager: FishProbabilityManager by inject()

    /**
     * 指定されたレアリティと釣り場に基づいて魚データを抽選する
     * 魚の出現条件（天気、ワールド）を考慮して抽選を行う
     *
     * @param rarity 抽選対象のレアリティ
     * @param fishingWorldId 釣り場のID
     * @return 抽選された魚データ
     */
    private fun drawRandomFishDataByRarity(
        rarity: RarityId,
        fishingWorldId: FishingWorldId,
    ): FishData {
        // 現在の天気を取得
        val weatherType = worldManager.getWorld(fishingWorldId).getCurrentWeather()
        // 条件に合致する魚データをフィルタリング
        val fishesData =
            fishManager.getFishesWithRarity(rarity).filter {
                it.isDisabled == false &&
                    (it.conditions.world.isEmpty() || it.conditions.world.contains(fishingWorldId)) &&
                    (it.conditions.weather.isEmpty() || it.conditions.weather.contains(weatherType))
            }
        // 重み付け抽選のための合計値を計算
        val total = fishesData.sumOf { it.weight }
        // 乱数を生成
        val randomValue = random.nextDouble() * total
        var sum = 0.0
        // 重み付け抽選を実行
        for (fish in fishesData) {
            sum += fish.weight
            if (randomValue <= sum) {
                return fish
            }
        }
        return fishesData.last()
    }

    /**
     * 指定されたレアリティに基づいて魚を抽選する
     *
     * @param rarity 抽選対象のレアリティ
     * @param fishingWorldId 釣り場のID
     * @return 抽選された魚
     */
    override fun selectRandomFishByRarity(
        rarity: RarityId,
        fishingWorldId: FishingWorldId,
    ): Fish {
        return selectRandomFishByFishData(drawRandomFishDataByRarity(rarity, fishingWorldId))
    }

    /**
     * 魚データに基づいて魚を抽選する
     * 魚のサイズを正規分布に基づいて決定する
     *
     * @param fishData 抽選対象の魚データ
     * @return 抽選された魚
     */
    override fun selectRandomFishByFishData(fishData: FishData): Fish {
        val (min, max) = fishData.size
        val mid = (min + max) / 2
        val standardDeviation = (max - min) / 6.0
        val random = ThreadLocalRandom.current().nextGaussian() * standardDeviation + mid
        val size = random.coerceIn(min, max)
        val fish =
            FishBuilderImpl.getBuilder()
                .fishData(fishData)
                .size(size)
                .build()
        return fish
    }

    /**
     * レアリティを抽選する
     * 各レアリティの出現確率に従って抽選を行う
     *
     * @return 抽選されたレアリティ
     */
    override fun drawRandomRarity(): RarityId {
        val rarities = rarityManager.getRarities()
        val total = rarities.sumOf { it.weight }
        val randomValue = random.nextDouble() * total
        var sum = 0.0
        for (rarity in rarities) {
            sum += rarity.weight
            if (randomValue <= sum) {
                return rarity.id
            }
        }
        return rarities.last().id
    }

    /**
     * アングラーのコンテキストを考慮してレアリティを抽選する
     * 確率修正値を適用して抽選を行う
     *
     * @param angler 対象のアングラー
     * @return 抽選されたレアリティ
     */
    override fun drawRandomRarity(angler: Angler): RarityId {
        val rarities = rarityManager.getRarities()

        // 各レアリティの基本重みを設定し、修正後重みを計算
        val modifiedRarities =
            rarities.map { rarity ->
                // 設定ファイルからの重みを基本重みとして設定
                fishProbabilityManager.setBaseRarityWeight(rarity.id, rarity.weight)
                val modifiedWeight = fishProbabilityManager.getModifiedRarityWeight(angler, rarity.id)
                rarity to modifiedWeight
            }

        val total = modifiedRarities.sumOf { it.second }
        if (total <= 0.0) {
            // すべての重みが0以下の場合はデフォルトロジックを使用
            return drawRandomRarity()
        }

        val randomValue = random.nextDouble() * total
        var sum = 0.0
        for ((rarity, weight) in modifiedRarities) {
            sum += weight
            if (randomValue <= sum) {
                return rarity.id
            }
        }
        return modifiedRarities.last().first.id
    }

    /**
     * アングラーのコンテキストを考慮して魚を抽選する
     * 確率修正値を適用して抽選を行う
     *
     * @param angler 対象のアングラー
     * @param fishingWorldId 釣り場のID
     * @return 抽選された魚
     */
    override fun selectRandomFish(
        angler: Angler,
        fishingWorldId: FishingWorldId,
    ): Fish {
        // 修正されたレアリティ抽選を使用
        val rarity = drawRandomRarity(angler)
        return selectRandomFishByRarity(rarity, fishingWorldId, angler)
    }

    /**
     * 指定されたレアリティに基づいて魚を抽選する（アングラーコンテキスト付き）
     * 確率修正値を適用して抽選を行う
     *
     * @param rarity 抽選対象のレアリティ
     * @param fishingWorldId 釣り場のID
     * @param angler 対象のアングラー
     * @return 抽選された魚
     */
    fun selectRandomFishByRarity(
        rarity: RarityId,
        fishingWorldId: FishingWorldId,
        angler: Angler,
    ): Fish {
        return selectRandomFishByFishData(drawRandomFishDataByRarity(rarity, fishingWorldId, angler))
    }

    /**
     * 指定されたレアリティと釣り場に基づいて魚データを抽選する（アングラーコンテキスト付き）
     * 魚の出現条件（天気、ワールド）と確率修正値を考慮して抽選を行う
     *
     * @param rarity 抽選対象のレアリティ
     * @param fishingWorldId 釣り場のID
     * @param angler 対象のアングラー
     * @return 抽選された魚データ
     */
    private fun drawRandomFishDataByRarity(
        rarity: RarityId,
        fishingWorldId: FishingWorldId,
        angler: Angler,
    ): FishData {
        // 現在の天気を取得
        val weatherType = worldManager.getWorld(fishingWorldId).getCurrentWeather()
        // 条件に合致する魚データをフィルタリング
        val fishesData =
            fishManager.getFishesWithRarity(rarity).filter {
                !it.isDisabled && (it.conditions.world.isEmpty() || it.conditions.world.contains(fishingWorldId)) && (it.conditions.weather.isEmpty() || it.conditions.weather.contains(weatherType))
            }

        // 各魚の修正後重みを計算
        val modifiedFishes =
            fishesData.map { fishData ->
                // 設定ファイルからの重みを基本重みとして設定
                fishProbabilityManager.setBaseFishWeight(fishData.id, fishData.weight)
                val modifiedWeight = fishProbabilityManager.getModifiedFishWeight(angler, fishData.id)
                fishData to modifiedWeight
            }

        val total = modifiedFishes.sumOf { it.second }
        if (total <= 0.0) {
            // すべての重みが0以下の場合はデフォルトロジックを使用
            return drawRandomFishDataByRarity(rarity, fishingWorldId)
        }

        // 重み付け抽選を実行
        val randomValue = random.nextDouble() * total
        var sum = 0.0
        for ((fishData, weight) in modifiedFishes) {
            sum += weight
            if (randomValue <= sum) {
                return fishData
            }
        }
        return modifiedFishes.last().first
    }
}
