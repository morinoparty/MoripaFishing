package party.morino.moripafishing.core.fish

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Random

class WeightedTest {
    @Test
    fun `an empty list gives null`() {
        assertNull(emptyList<String>().pickWeighted(Random(1)) { 1.0 })
    }

    @Test
    fun `all-zero weights give null`() {
        assertNull(listOf("a", "b").pickWeighted(Random(1)) { 0.0 })
    }

    @Test
    fun `a zero-weight element is never picked`() {
        val weights = mapOf("zero" to 0.0, "one" to 1.0, "two" to 2.0)
        val random = Random(7)
        repeat(10_000) { assertNotEquals("zero", weights.keys.toList().pickWeighted(random) { weights.getValue(it) }) }
    }

    @Test
    fun `frequencies follow the weights`() {
        val weights = mapOf("a" to 1.0, "b" to 3.0, "c" to 6.0)
        val random = Random(42)
        val draws = 100_000
        val counts = (1..draws).groupingBy { weights.keys.toList().pickWeighted(random) { weights.getValue(it) } }.eachCount()
        weights.forEach { (key, weight) ->
            assertEquals(weight / 10.0, counts.getValue(key).toDouble() / draws, 0.01, "frequency of $key")
        }
    }
}
