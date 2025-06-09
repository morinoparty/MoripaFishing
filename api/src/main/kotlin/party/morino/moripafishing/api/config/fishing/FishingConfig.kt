package party.morino.moripafishing.api.config.fishing

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.model.rod.RodConfiguration

/**
 * 釣りの設定を保持するデータクラス
 */
@Serializable
data class FishingConfig(
    val baseWaitTime: BaseWaitTimeConfig = BaseWaitTimeConfig(),
    val rodTemplates: Map<String, RodConfiguration> = emptyMap(),
    val enchantmentEffects: EnchantmentEffects = EnchantmentEffects(),
    @Deprecated("Legacy field, will be removed")
    val test: String = "test",
)
