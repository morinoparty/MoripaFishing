package party.morino.moripafishing.api.model.angler

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.utils.serializer.UUIDSerializer
import java.util.*

/**
 * 釣り人のIDを表すデータクラス
 * @property uuid プレイヤーのUUID
 */
@Serializable
data class AnglerId(
    val uuid: @Serializable(with = UUIDSerializer::class) UUID
)
