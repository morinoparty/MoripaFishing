package party.morino.moripafishing.api.config.world

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.serialization.LocaleSerializer
import java.util.Locale

/**
 * 釣りワールドごとの設定 (`world/<id>.json`)。このファイルが存在する ID が「定義済み」の釣りワールドになる。
 *
 * @property id ワールド ID。ファイル名 (`<id>.json`) と一致すること
 * @property name ロケール別の表示名
 * @property world 対応する Bukkit ワールド。MoripaFishing が管理するワールドか、既存の外部ワールドか
 * @property climate 気候設定。`null` の場合、管理ワールドは `WorldConfig.defaultClimate`、
 *   外部ワールドはバニラの天候・時刻を読み取る設定 (`WeatherPolicy.Vanilla` / `TimePolicy.Vanilla`) を使う
 */
@Serializable
data class FishingWorldConfig(
    val id: FishingWorldId,
    val name: Map<
        @Serializable(with = LocaleSerializer::class)
        Locale,
        String,
    > = emptyMap(),
    val world: WorldBinding = WorldBinding.Managed(),
    val climate: ClimateConfig? = null,
)
