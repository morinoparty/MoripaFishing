package party.morino.moripafishing.core.angler

import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.model.angler.AnglerId
import java.util.*

/**
 * 釣り人マネージャーの実装クラス
 */
class AnglerManagerImpl : AnglerManager, KoinComponent {
    override fun getAnglerByUUID(uuid: UUID): Angler {
            return AnglerImpl(uuid)
    }

    override fun getAnglerById(anglerId: AnglerId): Angler {
        return AnglerImpl(anglerId.uuid)
    }

    override fun getOnlineAnglers(): List<Angler> {
        val players = Bukkit.getOnlinePlayers()
        return players.map { player ->
            AnglerImpl(player.uniqueId)
        }
    }
} 