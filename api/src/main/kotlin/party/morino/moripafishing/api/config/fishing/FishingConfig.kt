package party.morino.moripafishing.api.config.fishing

import kotlinx.serialization.Serializable

/**
 * 釣りの設定を保持するデータクラス
 * エンチャント効果はハードコードされた値を使用する
 * ロッドプリセットはリソースファイルから読み込む
 */
@Serializable
data class FishingConfig(
    val baseWaitTime: BaseWaitTimeConfig = BaseWaitTimeConfig()
)
