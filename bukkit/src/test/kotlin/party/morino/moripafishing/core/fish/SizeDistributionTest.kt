package party.morino.moripafishing.core.fish

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import party.morino.moripafishing.api.model.fish.FishSize
import java.util.Random

class SizeDistributionTest {
    private val size = FishSize(10.0, 40.0)

    @Test
    fun `lengthRate of the mean is one half`() {
        assertEquals(0.5, SizeDistribution.lengthRate(size, 25.0), 1e-6)
    }

    @Test
    fun `lengthRate is monotonic`() {
        val rates = (0..30).map { SizeDistribution.lengthRate(size, size.min + it) }
        rates.zipWithNext().forEach { (a, b) -> assertTrue(a < b, "$a should be less than $b") }
        assertTrue(rates.first() < 0.01)
        assertTrue(rates.last() > 0.99)
    }

    @Test
    fun `samples stay within the range`() {
        val random = Random(3)
        repeat(10_000) {
            val sample = SizeDistribution.sample(size, random)
            assertTrue(sample in size.min..size.max, "$sample is out of range")
        }
    }
}
