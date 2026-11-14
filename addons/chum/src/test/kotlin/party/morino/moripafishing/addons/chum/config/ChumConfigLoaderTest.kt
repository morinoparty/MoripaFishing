package party.morino.moripafishing.addons.chum.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockbukkit.mockbukkit.MockBukkitExtension

// Material の検証にサーバーが必要なため MockBukkit を使う
@ExtendWith(MockBukkitExtension::class)
class ChumConfigLoaderTest {
    private fun chumYaml(body: String) =
        """
        chums:
          test:
            item:
              material: COD
              name: "<aqua>Chum"
        """.trimIndent() + "\n" + body.trimIndent().prependIndent("    ") + "\n"

    @Test
    fun `the bundled config is valid`() {
        val text = checkNotNull(javaClass.getResource("/config.yml")).readText()
        val config = ChumConfigLoader.load(text)
        assertEquals(ChumConfig.DEFAULT_MAX_MULTIPLIER, config.maxMultiplier)
        assertEquals(ChumScope.AREA, config.chums.getValue("rare_chum").scope)
        assertEquals(mapOf("golden_carp" to 5.0), config.chums.getValue("golden_bait").fishWeights)
    }

    @Test
    fun `an empty file uses the defaults`() {
        assertEquals(ChumConfig(), ChumConfigLoader.load("{}"))
    }

    @Test
    fun `a full definition is parsed`() {
        val config =
            ChumConfigLoader.load(
                chumYaml(
                    """
                    scope: ANGLER
                    charges: 3
                    durationSeconds: 60
                    rarityMultipliers:
                      rare: 2.0
                    fishMultipliers:
                      tuna: 0.5
                    fishWeights:
                      golden_carp: 5
                    """,
                ),
            )
        val chum = config.chums.getValue("test")
        assertEquals(3, chum.charges)
        assertEquals(mapOf("rare" to 2.0), chum.rarityMultipliers)
        assertEquals(mapOf("tuna" to 0.5), chum.fishMultipliers)
        assertEquals(mapOf("golden_carp" to 5.0), chum.fishWeights)
    }

    @Test
    fun `an unknown key is rejected`() {
        assertThrows<IllegalArgumentException> { ChumConfigLoader.load("unknown: true\n") }
    }

    @ParameterizedTest
    @ValueSource(strings = [".nan", ".inf", "-1.0"])
    fun `multipliers and weights must be finite and not negative`(value: String) {
        val error =
            assertThrows<IllegalArgumentException> {
                ChumConfigLoader.load(chumYaml("scope: WORLD\ndurationSeconds: 60\nfishWeights:\n  tuna: $value"))
            }
        assertTrue("chums.test.fishWeights.tuna" in error.message.orEmpty(), error.message)
    }

    @ParameterizedTest
    @ValueSource(strings = [".nan", ".inf", "0.5"])
    fun `maxMultiplier must be finite and at least 1`(value: String) {
        assertThrows<IllegalArgumentException> { ChumConfigLoader.load("maxMultiplier: $value\n") }
    }

    @Test
    fun `scope specific fields are validated`() {
        val cases =
            mapOf(
                "scope: AREA\ndurationSeconds: 60" to "radius is required",
                "scope: AREA\nradius: 0\ndurationSeconds: 60" to "radius must be finite and positive",
                "scope: WORLD\nradius: 5\ndurationSeconds: 60" to "radius is only allowed",
                "scope: WORLD\ncharges: 1\ndurationSeconds: 60" to "charges is only allowed",
                "scope: ANGLER\ncharges: 0\ndurationSeconds: 60" to "charges must be positive",
                "scope: ANGLER\ndurationSeconds: 0" to "durationSeconds must be positive",
            )
        cases.forEach { (body, expected) ->
            val error = assertThrows<IllegalArgumentException> { ChumConfigLoader.load(chumYaml(body)) }
            assertTrue(expected in error.message.orEmpty(), "$body -> ${error.message}")
        }
    }

    @Test
    fun `item materials and ids are validated`() {
        val material =
            assertThrows<IllegalArgumentException> {
                ChumConfigLoader.load(chumYaml("scope: ANGLER\ndurationSeconds: 60").replace("COD", "NOT_A_MATERIAL"))
            }
        assertTrue("item.material" in material.message.orEmpty())
        val id =
            assertThrows<IllegalArgumentException> {
                ChumConfigLoader.load(chumYaml("scope: ANGLER\ndurationSeconds: 60").replace("  test:", "  Bad Id:"))
            }
        assertTrue("id must consist" in id.message.orEmpty())
    }
}
