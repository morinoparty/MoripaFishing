package party.morino.moripafishing.api.config.world

import java.util.Locale
import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.LocationData
import party.morino.moripafishing.api.model.world.generator.GeneratorId
import party.morino.moripafishing.api.utils.serializer.LocaleSerializer

// ワールドの詳細設定を保持するデータクラス
@Serializable
data class WorldDetailConfig(
        // ワールドのID
        val id: @Serializable FishingWorldId = FishingWorldId("default"),
        // ワールドの名前
        val name: Map<@Serializable(with = LocaleSerializer::class) Locale, String> =
                mapOf(
                        Locale.JAPAN to "つりとぴあ",
                ),
        // ワールドの境界サイズ
        val borderSize: Double? = null,
        // ワールドの中心座標
        val borderCentral: Pair<Double, Double> = Pair(0.0, 0.0),
        // スポーン地点の座標
        val spawnLocation: LocationData = LocationData(id, 0.0, 64.0, 0.0, 90.0, 0.0),
        // 天気設定
        val climateConfig: ClimateConfig? = null,
        // ワールドのジェネレータ
        val generator: GeneratorId = GeneratorId("void"),
)