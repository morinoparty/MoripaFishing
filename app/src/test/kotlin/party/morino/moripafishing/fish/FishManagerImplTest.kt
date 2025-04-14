package party.morino.moripafishing.fish

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.moripafishing.MoripaFishingTest
import party.morino.moripafishing.api.fish.FishManager

@ExtendWith(MoripaFishingTest::class)
class FishManagerImplTest: KoinTest {
    val fishManager: FishManager by inject()

    @Test
    fun getFish() {
        val fishes = fishManager.getFish()
        println(fishes)
    }

    @Test
    fun getFishesWithRarity() {
    }

}