package party.morino.moripafishing.api.config.world

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.SpawnPoint
import party.morino.moripafishing.api.serialization.LocaleSerializer
import java.util.Locale

/**
 * 釣りワールドごとの設定 (`world/<id>.json`)。このファイルが存在する ID が「定義済み」の釣りワールドになる。
 *
 * 現状、対応する Bukkit ワールドは常に `moripafishing:<id>` である。
 * 外部ワールドへの対応 (#334) では、既存ワールドを指す `worldKey` フィールドを追加する予定。
 *
 * @property id ワールド ID。ファイル名 (`<id>.json`) と一致すること
 * @property name ロケール別の表示名
 * @property generator ワールド作成時に使うジェネレーター ID (`WorldLifecycle` が提供する ID)。作成時のみ参照する
 * @property spawn スポーン地点
 * @property border ワールドボーダー。`null` の場合 MoripaFishing はボーダーを管理しない
 * @property climate 気候設定。`null` の場合は `WorldConfig.defaultClimate` を使う
 */
@Serializable
data class FishingWorldConfig(
    val id: FishingWorldId,
    val name: Map<
        @Serializable(with = LocaleSerializer::class)
        Locale,
        String,
    > = emptyMap(),
    val generator: String = "void",
    val spawn: SpawnPoint = DEFAULT_SPAWN,
    val border: BorderConfig? = BorderConfig(),
    val climate: ClimateConfig? = null,
)

/** 既定のスポーン地点 (原点の Y=64、西向き)。 */
private val DEFAULT_SPAWN = SpawnPoint(0.0, 64.0, 0.0, 90f, 0f)
