package party.morino.moripafishing.api.core.fishing

/**
 * エンチャント効果の定数定義
 * マインクラフト標準の効果に準拠したハードコードされた値を使用する
 */
object EnchantmentEffects {
    /**
     * 入れ食い（Lure）エンチャントの効果を取得
     * マインクラフト標準: レベルあたり5秒短縮
     *
     * @param level エンチャントレベル（1-3）
     * @return 待ち時間短縮効果のApplyValue、レベルが無効な場合はnull
     */
    fun getLureEffect(level: Int): ApplyValue? {
        return when (level) {
            1 -> ApplyValue(ApplyType.ADD, -5.0, "seconds") // 5秒短縮
            2 -> ApplyValue(ApplyType.ADD, -10.0, "seconds") // 10秒短縮
            3 -> ApplyValue(ApplyType.ADD, -15.0, "seconds") // 15秒短縮
            else -> null // 無効なレベル
        }
    }

    /**
     * 海運（Luck of the Sea）エンチャントの効果を取得
     * マインクラフト標準: 待ち時間には影響しない（ルート確率のみ変更）
     *
     * 注意: このエンチャントは本来は待ち時間に影響しないが、
     * 既存のプラグインとの互換性のため、わずかな短縮効果を適用
     *
     * @param level エンチャントレベル（1-3）
     * @return 軽微な待ち時間短縮効果のApplyValue、レベルが無効な場合はnull
     */
    fun getLuckOfTheSeaEffect(level: Int): ApplyValue? {
        return when (level) {
            1 -> ApplyValue(ApplyType.ADD, -0.5, "seconds") // 0.5秒短縮
            2 -> ApplyValue(ApplyType.ADD, -1.0, "seconds") // 1.0秒短縮
            3 -> ApplyValue(ApplyType.ADD, -1.5, "seconds") // 1.5秒短縮
            else -> null // 無効なレベル
        }
    }

    /**
     * サポートされているLureエンチャントの最大レベル
     */
    const val MAX_LURE_LEVEL = 3

    /**
     * サポートされているLuck of the Seaエンチャントの最大レベル
     */
    const val MAX_LUCK_OF_THE_SEA_LEVEL = 3
}
