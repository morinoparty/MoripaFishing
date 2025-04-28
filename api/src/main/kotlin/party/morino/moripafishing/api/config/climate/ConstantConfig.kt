package party.morino.moripafishing.api.config.climate

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 固定の天候・時間設定を保持するデータクラス
 *
 * @property weather 固定の天候（nullの場合は天候が変化する）
 * @property dayCycle 固定の時間（一日を24 * 4で割った値、nullの場合は時間が変化する）
 */
@Serializable
data class ConstantConfig(
    val weather: WeatherType? = null,
    val dayCycle: Int? = null,
) 