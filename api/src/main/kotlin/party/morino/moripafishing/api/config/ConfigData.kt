package party.morino.moripafishing.api.config

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.database.DatabaseConfig
import party.morino.moripafishing.api.config.fishing.FishingConfig
import party.morino.moripafishing.api.config.world.SpawnConfig
import party.morino.moripafishing.api.config.world.WorldConfig
import java.util.Locale
import party.morino.moripafishing.api.utils.serializer.LocaleSerializer
/**
 * プラグイン全体の設定データを保持するデータクラス
 *
 * @param database データベース関連の設定
 * @param world ワールド関連の設定
 * @param fishing 釣り関連の設定
 * @param spawn スポーン関連の設定
 */
@Serializable
data class ConfigData(
    // データベース設定
    val database: DatabaseConfig = DatabaseConfig(),
    // ワールド設定
    val world: WorldConfig = WorldConfig(),
    // 釣り設定
    val fishing: FishingConfig = FishingConfig(),
    // スポーン設定
    val spawn: SpawnConfig = SpawnConfig(),

    val defaultLocale: @Serializable(with = LocaleSerializer::class) Locale = Locale.JAPAN
)
