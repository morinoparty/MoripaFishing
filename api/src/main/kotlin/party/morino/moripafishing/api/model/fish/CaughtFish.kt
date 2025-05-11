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
 */
@Serializable
// 魚の捕獲情報を保持するdata class
// FishId, サイズ, レアリティ, 価値, CFD, 釣り人, ワールド, 捕獲時刻を持つ
// 釣果履歴やランキング等の用途で利用される
// UUIDはFishIdで一意に管理する
// ZonedDateTimeはISO8601(ex. 2025-05-11T00:00:00+09:00)でシリアライズされる
// AnglerId, FishingWorldId, RarityIdはそれぞれのmodel参照
// FishIdは魚種の識別子
// worthは価値、cfdは累積分布関数値
// timestampは捕獲日時
// worldは釣り場
// anglerは釣り人
// rarityはレアリティ
// sizeは魚のサイズ
// fishは魚種ID
//
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
        ): CaughtFish {
            return CaughtFish(
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
}
