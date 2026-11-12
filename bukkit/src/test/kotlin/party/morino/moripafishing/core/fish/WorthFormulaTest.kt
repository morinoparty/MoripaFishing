package party.morino.moripafishing.core.fish

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WorthFormulaTest {
    @Test
    fun `evaluates with length_rate`() {
        assertEquals(6.25, WorthFormula("(length_rate ^ 2) * 5 + 5").evaluate(0.5), 1e-9)
    }

    @Test
    fun `bad syntax is rejected`() {
        assertThrows<IllegalArgumentException> { WorthFormula("(length_rate * 2") }
        assertThrows<IllegalArgumentException> { WorthFormula("length_rate *") }
    }

    @Test
    fun `an unknown variable is rejected`() {
        assertThrows<IllegalArgumentException> { WorthFormula("size * 2") }
    }
}
