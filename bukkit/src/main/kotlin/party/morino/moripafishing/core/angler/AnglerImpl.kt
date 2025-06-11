package party.morino.moripafishing.core.angler

import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.Material
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fishing.rod.RodPresetManager
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.rod.RodConfiguration
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Location
import party.morino.moripafishing.utils.rod.RodAnalyzer
import java.util.UUID

class AnglerImpl(
    private val uniqueId: UUID,
) : Angler, KoinComponent {
    val plugin: MoripaFishing by inject()
    val worldManager: WorldManager by inject()
    val rodPresetManager: RodPresetManager by inject()

    /**
     * 釣り人のIDを取得する
     * @return 釣り人のID
     */
    override fun getAnglerUniqueId(): AnglerId {
        return AnglerId(uniqueId)
    }

    override fun getMinecraftUniqueId(): UUID {
        return uniqueId
    }

    override fun getName(): String {
        // オフラインプレイヤーかもしれないので、getOfflinePlayerを使う
        // nameがnullの場合は "Unknown" を返す
        return Bukkit.getOfflinePlayer(uniqueId).name ?: "Unknown"
    }

    override fun getWorld(): FishingWorld? {
        val offlinePlayer = Bukkit.getOfflinePlayer(uniqueId)
        if (!offlinePlayer.isOnline) return null
        val player = offlinePlayer.player ?: return null
        val world = worldManager.getWorld(FishingWorldId(player.world.name))

        return world
    }

    override fun getLocation(): Location? {
        val world = getWorld() ?: return null
        val player = Bukkit.getPlayer(uniqueId) ?: return null
        val location =
            Location(
                world.getId(),
                player.location.x,
                player.location.y,
                player.location.z,
                player.location.yaw.toDouble(),
                player.location.pitch.toDouble(),
            )
        return location
    }

    override fun getCurrentRodConfiguration(): RodConfiguration? {
        // プレイヤーがオンラインかチェック
        val player = Bukkit.getPlayer(uniqueId) ?: return null

        // プレイヤーのメインハンドまたはオフハンドに釣り竿があるかチェック
        val mainHandItem = player.inventory.itemInMainHand
        val offHandItem = player.inventory.itemInOffHand

        val rodItem =
            when {
                mainHandItem.type == Material.FISHING_ROD -> mainHandItem
                offHandItem.type == Material.FISHING_ROD -> offHandItem
                else -> return null
            }

        return runBlocking {
            // RodAnalyzerを使用してロッドの設定を分析・取得
            val rodAnalyzer = RodAnalyzer(plugin)
            // 最初にカスタムロッドの設定があるかチェック
            val customConfig = rodAnalyzer.analyzeRod(rodItem)
            if (customConfig != null) {
                customConfig
            } else {
                // カスタム設定がない場合、バニララッドのエンチャント効果を確認
                val vanillaConfig = rodAnalyzer.analyzeVanillaRod(rodItem)
                if (vanillaConfig != null) {
                    vanillaConfig
                } else {
                    // どちらもない場合はプリセットから基本の釣り竿を取得
                    rodPresetManager.getPreset("beginner") // デフォルトのビギナー用ロッド
                }
            }
        }
    }
}
