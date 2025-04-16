package party.morino.moripafishing.core.angler

import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.model.angler.AnglerId
import java.util.*

/**
 * 釣り人を管理する実装クラス
 */
class AnglerManagerImpl : AnglerManager, KoinComponent {


    /**
     * UUIDから釣り人を取得する
     * @param uuid プレイヤーのUUID
     * @return 釣り人（存在しない場合はnull）
     */
    override fun getAnglerByUUID(uuid: UUID): Angler? {
        return AnglerImpl(uuid)
    }

    /**
     * 釣り人IDから釣り人を取得する
     * @param anglerId 釣り人ID
     * @return 釣り人（存在しない場合はnull）
     */
    override fun getAnglerById(anglerId: AnglerId): Angler? {
        return AnglerImpl(anglerId.uuid)
    }

    /**
     * 全ての釣り人を取得する
     * @return 釣り人のリスト
     */
    override fun getOnlineAnglers(): List<Angler> {
        val onlinePlayers = Bukkit.getOnlinePlayers()
        return onlinePlayers.map { AnglerImpl(it.uniqueId) }
    }


} 