package party.morino.moripafishing.api.config.world

import java.util.Locale
import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.generator.GeneratorId
import party.morino.moripafishing.api.utils.serializer.LocaleSerializer

/**
 * ワールドの詳細設定を保持するデータクラス
 *
 * @property id ワールドのID
 * @property name ワールドの名前（ロケール別）
 * @property enableBorder プラグインによるワールドボーダー管理を有効にするかどうか。
 *   `false` の場合、ボーダーサイズ・中心の自動設定をスキップし、手動管理またはバニラのデフォルトに委ねる。
 * @property borderSize ワールドの境界サイズ（`null` の場合はデフォルトサイズを使用）
 * @property borderCentral ワールドの中心座標
 * @property spawnLocation スポーン地点の座標
 * @property climateConfig 天気設定（`null` の場合はデフォルト設定を使用）
 * @property generator ワールドのジェネレータ
 */
@Serializable
data class WorldDetailConfig(
        val id: @Serializable FishingWorldId = FishingWorldId("default"),
        val name: Map<@Serializable(with = LocaleSerializer::class) Locale, String> =
                mapOf(
                        Locale.JAPAN to "つりとぴあ",
                ),
        val enableBorder: Boolean = true,
        val borderSize: Double? = null,
        val borderCentral: Pair<Double, Double> = Pair(0.0, 0.0),
        val spawnLocation: LocationData = LocationData(id, 0.0, 64.0, 0.0, 90.0, 0.0),
        val climateConfig: ClimateConfig? = null,
        val generator: GeneratorId = GeneratorId("void"),
)