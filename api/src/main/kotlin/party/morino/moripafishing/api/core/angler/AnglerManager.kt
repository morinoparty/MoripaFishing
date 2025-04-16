package party.morino.moripafishing.api.core.angler

import party.morino.moripafishing.api.model.angler.AnglerId
import java.util.*

/**
 * 釣り人を管理するインターフェース
 */
interface AnglerManager {

    /**
     * UUIDから釣り人を取得する
     * @param uuid プレイヤーのUUID
     * @return 釣り人（存在しない場合はNone）
     */
    fun getAnglerByUUID(uuid: UUID): Angler?
    
    /**
     * 釣り人IDから釣り人を取得する
     * @param anglerId 釣り人ID
     * @return 釣り人（存在しない場合はNone）
     */
    fun getAnglerById(anglerId: AnglerId): Angler?

    /**
     * 全ての釣り人を取得する
     * @return 釣り人のリスト
     */
    fun getOnlineAnglers(): List<Angler>
}
