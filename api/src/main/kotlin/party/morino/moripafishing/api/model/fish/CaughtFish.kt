package party.morino.moripafishing.api.model.fish

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.serialization.UUIDSerializer
import party.morino.moripafishing.api.serialization.ZonedDateTimeSerializer
import java.time.ZonedDateTime
import java.util.UUID

/**
 * 釣果。釣りのイベントと魚アイテム (PDC `moripafishing:caught_fish`) が運ぶ。
 *
 * @property uniqueId この釣果の一意な ID
 * @property fish 魚の ID
 * @property rarity レアリティの ID
 * @property size サイズ (cm)
 * @property lengthRate サイズの累積分布関数の値 (0..1)
 * @property worth 価値
 * @property angler 釣り人の ID
 * @property world 釣り上げた釣りワールドの ID
 * @property timestamp 釣り上げた日時 (ISO-8601 で直列化する)
 */
@Serializable
data class CaughtFish(
    @Serializable(with = UUIDSerializer::class)
    val uniqueId: UUID,
    val fish: FishId,
    val rarity: RarityId,
    val size: Double,
    val lengthRate: Double,
    val worth: Double,
    val angler: AnglerId,
    val world: FishingWorldId,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val timestamp: ZonedDateTime,
) {
    companion object {
        /**
         * 抽選済みの魚から釣果を作る。`uniqueId` は `UUID.randomUUID()`、`timestamp` は `ZonedDateTime.now()`。
         *
         * @param fish 抽選済みの魚
         * @param angler 釣り人の ID
         * @param world 釣り上げた釣りワールドの ID
         */
        @JvmStatic
        fun of(
            fish: Fish,
            angler: AnglerId,
            world: FishingWorldId,
        ): CaughtFish =
            CaughtFish(
                uniqueId = UUID.randomUUID(),
                fish = fish.data.id,
                rarity = fish.rarity.id,
                size = fish.size,
                lengthRate = fish.lengthRate,
                worth = fish.worth,
                angler = angler,
                world = world,
                timestamp = ZonedDateTime.now(),
            )
    }
}
