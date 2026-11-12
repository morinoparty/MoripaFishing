package party.morino.moripafishing.api.model.angler

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.serialization.AnglerIdSerializer
import java.util.UUID

/**
 * 釣り人の ID。プレイヤーの Minecraft UUID と同一で、JSON では UUID 文字列になる。
 *
 * @property value プレイヤーの UUID
 */
@Serializable(with = AnglerIdSerializer::class)
data class AnglerId(
    val value: UUID,
)
