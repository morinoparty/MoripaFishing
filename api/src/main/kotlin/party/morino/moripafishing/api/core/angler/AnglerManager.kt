package party.morino.moripafishing.api.core.angler

import party.morino.moripafishing.api.model.angler.AnglerId
import java.util.UUID

/**
 * 釣り人を管理するインターフェース
 */
interface AnglerManager {

    /**
     * UUIDから釣り人を取得する
     * @param uuid プレイヤーのUUID
     * @return 釣り人（存在しない場合はNone）
     */
    fun getAnglerByUUID(uuid: UUID): Angler
    
    /**
     * 釣り人IDから釣り人を取得する
     * @param anglerId 釣り人ID
     * @return 釣り人（存在しない場合はNone）
     */
    fun getAnglerById(anglerId: AnglerId): Angler

    /**
     * プレイヤーから釣り人を取得する
     * @param player プレイヤー
     * @return 釣り人（存在しない場合はNone）
     */
    fun getOnlineAnglers(): List<Angler>
} 