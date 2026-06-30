package party.morino.moripafishing.integrations.worldlifecycle.api

import kotlinx.serialization.Serializable

/**
 * ワールドジェネレータの定義。
 *
 * `WorldLifecycleProvider` を通じて参照・登録される。
 * `:api` には依存しない独立した値型。
 *
 * @param id ジェネレータ ID (例: `"terra"`, `"void"`, `"normal"`)
 * @param generator `WorldCreator.generator(...)` に渡す文字列 (プラグイン名:ID)
 * @param type `WorldType` 名 (`NORMAL` / `FLAT` / `LARGE_BIOMES` / `AMPLIFIED`)
 * @param biomeProvider 固定バイオーム名 (例: `"minecraft:plains"`)
 * @param generatorSetting ジェネレータ固有の設定文字列
 */
@Serializable
data class GeneratorData(
    val id: String,
    val generator: String? = null,
    val type: String? = null,
    val biomeProvider: String? = null,
    val generatorSetting: String? = null,
)
