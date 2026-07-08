package party.morino.moripafishing.api.model.fish

import com.github.f4b6a3.uuid.UuidCreator
import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fish.Fish
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.rarity.RarityId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.utils.serializer.UUIDSerializer
import party.morino.moripafishing.api.utils.serializer.ZonedDateTimeSerializer
import java.time.ZonedDateTime
import java.util.UUID

/**
 * 釣り上げた魚の情報を表すデータクラス
 * DB保存やAPI返却用の純粋なデータ構造
 *
 * @property uniqueId この釣果の一意な識別子
 * @property fish 魚種のID
 * @property size 魚のサイズ
 * @property rarity レアリティのID
 * @property worth 価値
 * @property cfd 累積分布関数値
 * @property angler 釣り人のID
 * @property world 釣り上げたワールドのID
 * @property timestamp 捕獲日時 (ISO8601でシリアライズされる)
 */
@Serializable
data class CaughtFish(
    @Serializable(with = UUIDSerializer::class)
    val uniqueId: UUID,
    val fish: FishId,
    val size: Double,
    val rarity: RarityId,
    val worth: Double,
    val cfd: Double,
    val angler: AnglerId,
    val world: FishingWorldId,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val timestamp: ZonedDateTime,
) {
    companion object {
        fun fromFish(
            fish: Fish,
            angler: Angler,
            world: FishingWorld,
        ): CaughtFish =
            CaughtFish(
                uniqueId = UuidCreator.getTimeOrderedEpoch(),
                fish = fish.getId(),
                size = fish.getSize(),
                rarity = fish.getRarity().id,
                worth = fish.getWorth(),
                cfd = fish.getCFD(),
                angler = angler.getAnglerUniqueId(),
                world = world.getId(),
                timestamp = ZonedDateTime.now(),
            )
    }
}
