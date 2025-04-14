package party.morino.moripafishing.random.fish

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.fish.Fish
import party.morino.moripafishing.api.fish.FishId
import party.morino.moripafishing.api.fish.FishManager
import party.morino.moripafishing.api.random.RandomizeManager
import party.morino.moripafishing.api.random.fish.FishRandomizer
import party.morino.moripafishing.api.rarity.RarityId
import kotlin.collections.map

@ExtendWith(MoripaFishingTest::class)
class FishRandomizerImplTest: KoinTest {
    val fishManager : FishManager by inject()
    val randomizeManager : RandomizeManager by inject()
    val fishRandomizer : FishRandomizer by lazy{
        randomizeManager.getFishRandomizer()
    }

    @Test
    fun getRandomFishWithFishData() {
        val list : ArrayList<Pair<Double, Double>> = arrayListOf()
        repeat(100) {
            val targetFish = fishManager.getFishWithId(FishId("red_jelly_fish")) ?: error("Fish not found")
            val fish : Fish = fishRandomizer.getRandomFishWithFishData(targetFish)
            list.add(Pair(fish.getSize(), fish.getWorth()))
        }
//        list.sortBy { it.first }
//        list.map { Pair(it.first.toFix(2), it.second.toFix(2)) }.forEach {
//            println("Size: ${it.first}, Worth: ${it.second}")
//        }
    }


    @Test
    @DisplayName("ランダムな魚を100匹生成し、サイズでソートして表示する")
    fun getRandomFish() {
        val list = arrayListOf<Fish>()
        repeat(100) {
            val fish : Fish = fishRandomizer.getRandomFishWithRarity(RarityId("common"))
            list.add(fish)
        }
        list.sortBy { it.getSize() }
        //count
        val count : MutableMap<FishId, Int> = mutableMapOf()
        list.forEach {
            val fishId = it.getId()
            count[fishId] = count.getOrDefault(fishId, 0) + 1
        }
        val rate = count.map { (k, v) -> "${k} : ${v.toDouble() / list.size * 100}" }
        println(rate)
    }

    @Test
    fun getRandomRarity() {
        val count : MutableMap<RarityId, Int> = mutableMapOf()
        val r = 100000
        repeat(r){
            val rarity = fishRandomizer.getRandomRarity()
            count[rarity] = count.getOrDefault(rarity, 0) + 1
        }
        val rate = count.map { (k, v) -> "${k} : ${v.toDouble() / r * 100}" }
        println(rate)
    }


    fun Double.toFix(decimal: Int): String {
        return "%.0${decimal}f".format(this)
    }

}