package party.morino.moripafishing.api.model.world.generator

import kotlinx.serialization.Serializable

/**
 * ワールドジェネレータの情報を表すデータクラス
 * @param id ジェネレータID
 * @param name ジェネレータ名
 * 今後パラメータ等を追加する場合はここに追記する
 */
@Serializable
data class GeneratorData(
    val id: GeneratorId,
    val generator: String? = null,
    val type: String? = null,
    val biomeProvider: String? = null,
    val generatorSetting: String? = null,
)
