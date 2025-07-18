package party.morino.moripafishing.core.random.fish

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.random.RandomizeManager
import party.morino.moripafishing.api.core.random.fish.FishRandomizer
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.fish.FishId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId

@ExtendWith(MoripaFishingTest::class)
class FishRandomizerImplTest : KoinTest {
    val fishManager: FishManager by inject()
    val randomizeManager: RandomizeManager by inject()
    val worldManager: WorldManager by inject()
    val fishRandomizer: FishRandomizer by lazy {
        randomizeManager.getFishRandomizer()
    }
    val fishingWorldId: FishingWorldId by lazy {
        worldManager.getDefaultWorldId()
    }

    @Test
    @DisplayName("FishRandomizer No.1: 魚データに基づいてランダムな魚を生成する")
    fun getRandomFishWithFishData() {
        val list: ArrayList<Pair<Double, Double>> = arrayListOf()
        repeat(100) {
            val targetFish = fishManager.getFishWithId(FishId("sillago_japonica")) ?: error("Fish not found")
            val fish: Fish = fishRandomizer.selectRandomFishByFishData(targetFish)
            list.add(Pair(fish.getSize(), fish.getWorth()))
        }
//        list.sortBy { it.first }
//        list.map { Pair(it.first.toFix(2), it.second.toFix(2)) }.forEach {
//            println("Size: ${it.first}, Worth: ${it.second}")
//        }
    }

    @Test
    @DisplayName("FishRandomizer No.2: ランダムな魚を10000匹生成し、サイズでソートして表示する")
    fun getRandomFishWithRarity() {
        val list = arrayListOf<Fish>()
        repeat(10000) {
            val fish: Fish = fishRandomizer.selectRandomFishByRarity(RarityId("common"), fishingWorldId)
            list.add(fish)
        }
        list.sortBy { it.getSize() }
        // count
        val count: MutableMap<FishId, Int> = mutableMapOf()
        list.forEach {
            val fishId = it.getId()
            count[fishId] = count.getOrDefault(fishId, 0) + 1
        }
//        val rate = count.map { (k, v) -> "${k.value.padEnd(25)} : ${v.toDouble() / list.size * 100}" }.joinToString("\n")
//        println(rate)
    }

    // 　acanthogobius_flavimanusにRAINYが設定されているので、天気による制限がある魚を10000匹生成し、表示する
    // ./gradlew test --tests "party.morino.moripafishing.core.random.fish.FishRandomizerImplTest.getRestrictFishWithWeather"
    @Test
    @DisplayName("FishRandomizer No.3: 天気による制限がある魚を10000匹生成し、表示する")
    fun getRestrictFishWithWeather() {
        val set = mutableSetOf<FishId>()
        repeat(10000) {
            val fish: Fish = fishRandomizer.selectRandomFishByRarity(RarityId("common"), fishingWorldId)
            set.add(fish.getId())
        }
        println(set.size)
    }

    @Test
    @DisplayName("FishRandomizer No.4: ワールドに基づいてランダムな魚を生成する")
    fun getRandomFishWithWorld() {
        val list = arrayListOf<Fish>()
        repeat(10000) {
            val fish: Fish = fishRandomizer.selectRandomFish(fishingWorldId)
            list.add(fish)
        }
        list.sortBy { it.getSize() }
        // count
        val count: MutableMap<RarityId, Int> = mutableMapOf()
        list.forEach {
            val rarityId = it.getRarity().id
            count[rarityId] = count.getOrDefault(rarityId, 0) + 1
        }
        val rate =
            count.toList().sortedByDescending {
                it.second
            }.joinToString("\n") { (k, v) -> "${k.value.padEnd(25)} : ${v.toDouble() / list.size * 100}" }
        println(rate)
    }

    @Test
    @DisplayName("FishRandomizer No.5: ランダムレアリティの抽選確率を検証する")
    fun getRandomRarity() {
        val count: MutableMap<RarityId, Int> = mutableMapOf()
        val r = 100000
        repeat(r) {
            val rarity = fishRandomizer.drawRandomRarity()
            count[rarity] = count.getOrDefault(rarity, 0) + 1
        }
        val rate = count.map { (k, v) -> "$k : ${v.toDouble() / r * 100}" }
        println(rate)
    }

    fun Double.toFix(decimal: Int): String {
        return "%.0${decimal}f".format(this)
    }
}
