package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import java.time.ZonedDateTime
import java.util.UUID

class CaughtFishTest {
    private val anglerUuid = UUID.fromString("0f4b4f3e-1b2a-4c5d-8e9f-0a1b2c3d4e5f")
    private val caught =
        CaughtFish(
            uniqueId = UUID.fromString("7d8e9f00-1111-4222-8333-444455556666"),
            fish = FishId("tuna"),
            rarity = RarityId("rare"),
            size = 123.45,
            lengthRate = 0.75,
            worth = 42.0,
            angler = AnglerId(anglerUuid),
            world = FishingWorldId("default"),
            timestamp = ZonedDateTime.parse("2026-11-12T09:30:00+09:00[Asia/Tokyo]"),
        )

    @Test
    fun `round-trips through JSON`() {
        val text = Json.encodeToString(CaughtFish.serializer(), caught)
        assertEquals(caught, Json.decodeFromString(CaughtFish.serializer(), text))
    }

    @Test
    fun `angler is a plain UUID string and lengthRate is present`() {
        val json = Json.parseToJsonElement(Json.encodeToString(CaughtFish.serializer(), caught)).jsonObject
        assertEquals(anglerUuid.toString(), json.getValue("angler").jsonPrimitive.content)
        assertTrue(json.getValue("angler").jsonPrimitive.isString)
        assertEquals(
            0.75,
            json
                .getValue("lengthRate")
                .jsonPrimitive.content
                .toDouble(),
        )
    }
}
