package party.morino.moripafishing.api.config

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.world.WorldConfig
import java.util.Locale
import party.morino.moripafishing.api.utils.serializer.LocaleSerializer
/**
 * プラグイン全体の設定データを保持するデータクラス
 *
 * @param world ワールド関連の設定
 * @param defaultLocale デフォルトのロケール
 */
@Serializable
data class ConfigData(
    // ワールド設定
    val world: WorldConfig = WorldConfig(),

    val defaultLocale: @Serializable(with = LocaleSerializer::class) Locale = Locale.JAPAN
)
