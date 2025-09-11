package party.morino.moripafishing.api.core.world

import party.morino.moripafishing.api.model.world.generator.GeneratorData
import party.morino.moripafishing.api.model.world.generator.GeneratorId

/**
 * ワールドジェネレータの管理を行うインターフェース
 */
interface GeneratorManager {
    /**
     * ジェネレータIDからGeneratorDataを取得する
     * @param id ジェネレータID
     * @return GeneratorData（見つからない場合はnull）
     */
    fun getGenerator(id: GeneratorId): GeneratorData?

    /**
     * 登録されている全ジェネレータを取得する
     */
    fun getAllGenerators(): List<GeneratorData>

    /**
     * ジェネレータを追加する
     * @param generator 追加するジェネレータ
     */
    fun addGenerator(generator: GeneratorData)
}
