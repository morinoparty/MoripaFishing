package party.morino.moripafishing.api.model.rod

import party.morino.moripafishing.api.core.fishing.ApplyType
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.fishing.EffectUnits

/**
 * RodConfigurationの拡張機能
 * Map<Regex, List<ApplyValue>>構造のbonusEffectsから効果を取得するためのヘルパー
 */

/**
 * 指定されたワールド名に対応する効果リストを取得
 * @param worldName ワールド名（nullの場合は全てのワールドにマッチする効果を取得）
 * @return マッチした効果のリスト
 */
fun RodConfiguration.getEffectsForWorld(worldName: String?): List<ApplyValue> {
    return bonusEffects
        .filterKeys { regex ->
            worldName?.let { regex.matches(it) } ?: (regex.pattern == ".*")
        }
        .values
        .flatten()
}

/**
 * 全ての効果を統合したリストを取得（ワールド関係なく全て）
 * @return 統合された効果のリスト
 */
fun RodConfiguration.getAllEffects(): List<ApplyValue> {
    return bonusEffects.values.flatten()
}

/**
 * 待機時間の倍率を取得 (従来のwaitTimeMultiplierに相当)
 * @param worldName ワールド名
 * @return 待機時間の倍率、設定されていない場合は1.0
 */
fun RodConfiguration.getWaitTimeMultiplier(worldName: String? = null): Double {
    return getEffectsForWorld(worldName)
        .firstOrNull { it.unit == EffectUnits.WAIT_TIME && it.type == ApplyType.MULTIPLY }
        ?.value ?: 1.0
}

/**
 * 天候免疫の状態を取得 (従来のweatherImmunityに相当)
 * @param worldName ワールド名
 * @return 天候免疫が有効かどうか
 */
fun RodConfiguration.hasWeatherImmunity(worldName: String? = null): Boolean {
    return getEffectsForWorld(worldName)
        .any { it.unit == EffectUnits.WEATHER_IMMUNITY }
}

/**
 * ワールド固有のボーナス一覧を取得 (従来のfishingWorldBonusesに相当)
 * @return ワールド名とボーナス倍率のマップ
 */
fun RodConfiguration.getFishingWorldBonuses(): Map<String, Double> {
    val result = mutableMapOf<String, Double>()
    bonusEffects.forEach { (regex, effects) ->
        effects.forEach { effect ->
            EffectUnits.extractWorldName(effect.unit)?.let { worldName ->
                result[worldName] = effect.value
            }
        }
    }
    return result
}

/**
 * 待機時間倍率の効果を追加したRodConfigurationを作成
 * @param multiplier 待機時間の倍率
 * @param worldPattern ワールドパターン（デフォルトは全ワールド）
 * @return 新しいRodConfiguration
 */
fun RodConfiguration.withWaitTimeMultiplier(
    multiplier: Double,
    worldPattern: String = ".*",
): RodConfiguration {
    if (multiplier == 1.0) return this

    val regex = worldPattern.toRegex()
    val currentEffects = bonusEffects[regex] ?: emptyList()

    // 既存の待機時間倍率効果を削除
    val filteredEffects =
        currentEffects.filterNot {
            it.unit == EffectUnits.WAIT_TIME && it.type == ApplyType.MULTIPLY
        }

    // 新しい効果を追加
    val newEffects = filteredEffects + ApplyValue(ApplyType.MULTIPLY, multiplier, EffectUnits.WAIT_TIME)

    return copy(bonusEffects = bonusEffects + (regex to newEffects))
}

/**
 * 天候免疫効果を追加したRodConfigurationを作成
 * @param immunity 天候免疫を有効にするかどうか
 * @param worldPattern ワールドパターン（デフォルトは全ワールド）
 * @return 新しいRodConfiguration
 */
fun RodConfiguration.withWeatherImmunity(
    immunity: Boolean,
    worldPattern: String = ".*",
): RodConfiguration {
    val regex = worldPattern.toRegex()
    val currentEffects = bonusEffects[regex] ?: emptyList()

    // 既存の天候免疫効果を削除
    val filteredEffects =
        currentEffects.filterNot {
            it.unit == EffectUnits.WEATHER_IMMUNITY
        }

    val newEffects =
        if (immunity) {
            filteredEffects + ApplyValue(ApplyType.CONSTANT, 0.0, EffectUnits.WEATHER_IMMUNITY)
        } else {
            filteredEffects
        }

    return copy(bonusEffects = bonusEffects + (regex to newEffects))
}

/**
 * ワールドボーナス効果を追加したRodConfigurationを作成
 * @param worldBonuses ワールド名とボーナス倍率のマップ
 * @return 新しいRodConfiguration
 */
fun RodConfiguration.withFishingWorldBonuses(worldBonuses: Map<String, Double>): RodConfiguration {
    var updatedEffects = bonusEffects.toMutableMap()

    worldBonuses.forEach { (worldName, bonus) ->
        if (bonus != 1.0) {
            val regex = worldName.toRegex()
            val currentEffects = updatedEffects[regex] ?: emptyList()

            // 既存のワールドボーナス効果を削除
            val filteredEffects =
                currentEffects.filterNot { effect ->
                    EffectUnits.extractWorldName(effect.unit) != null
                }

            // 新しいワールドボーナス効果を追加
            val newEffects = filteredEffects + ApplyValue(ApplyType.MULTIPLY, bonus, EffectUnits.worldBonus(worldName))
            updatedEffects[regex] = newEffects
        }
    }

    return copy(bonusEffects = updatedEffects)
}
