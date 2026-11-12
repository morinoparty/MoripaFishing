package party.morino.moripafishing.api.config

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.config.world.WorldConfig
import party.morino.moripafishing.api.serialization.LocaleSerializer
import java.util.Locale

/**
 * プラグイン全体の設定 (`config.json`)。
 *
 * @property world 釣りワールド全体に関する設定
 * @property defaultLocale 翻訳が見つからない場合に使うロケール
 */
@Serializable
data class ConfigData(
    val world: WorldConfig = WorldConfig(),
    val defaultLocale:
        @Serializable(with = LocaleSerializer::class)
        Locale = Locale.JAPAN,
)
