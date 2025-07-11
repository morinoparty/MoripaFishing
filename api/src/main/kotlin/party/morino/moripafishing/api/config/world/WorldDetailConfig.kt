package party.morino.moripafishing.api.config.world

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.api.model.world.generator.GeneratorId

// ワールドの詳細設定を保持するデータクラス
@Serializable
data class WorldDetailConfig(
        // ワールドのID
        val id: @Serializable FishingWorldId = FishingWorldId("default"),
        // ワールドの名前
        val name: String = "つりとぴあ",
        // ワールドの境界サイズ
        val borderSize: Double? = null,
        // ワールドの中心座標
        val borderCentral: Pair<Double, Double> = Pair(0.0, 0.0),
        // スポーン地点の座標
        val spawnLocation: Location = Location(id, 0.0, 64.0, 0.0, 90.0, 0.0),
        // 天気設定
        val climateConfig: ClimateConfig? = null,
        // ワールドのジェネレータ
        val generator: GeneratorId = GeneratorId("void"),
)
