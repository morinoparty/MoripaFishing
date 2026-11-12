package party.morino.moripafishing.integrations.worldlifecycle.generator

import kotlinx.serialization.Serializable

/**
 * ワールドジェネレーターの定義 (`generator/<id>.json`)。本 Integration の内部形式。
 *
 * @param id ジェネレーター ID (例: `"terra"`, `"void"`, `"normal"`)
 * @param generator `WorldCreator.generator(...)` に渡す文字列 (プラグイン名:ID)
 * @param type `WorldType` 名 (`NORMAL` / `FLAT` / `LARGE_BIOMES` / `AMPLIFIED`)
 * @param biomeProvider 固定バイオーム名 (例: `"minecraft:plains"`)
 * @param generatorSetting ジェネレーター固有の設定文字列
 */
@Serializable
internal data class GeneratorData(
    val id: String,
    val generator: String? = null,
    val type: String? = null,
    val biomeProvider: String? = null,
    val generatorSetting: String? = null,
)
