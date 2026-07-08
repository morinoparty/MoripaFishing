package party.morino.moripafishing.core.world.weather

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.MoripaFishing

/**
 * 無効化されたプラグインが登録した `WeatherSource` を自動解除するリスナー。
 *
 * 解除により、該当ソースを使用中のワールドは `moripafishing:internal` へフォールバックし、
 * 無効化済みプラグインのプロバイダーが呼ばれ続けることを防ぐ。
 */
class WeatherSourceCleanupListener :
    Listener,
    KoinComponent {
    private val plugin: MoripaFishing by inject()
    private val weatherSourceRegistry: WeatherSourceRegistry by inject()

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        // 自プラグインの無効化時は onDisable 側で後始末するため対象外
        if (event.plugin === plugin) return
        weatherSourceRegistry.unregisterOwnedBy(event.plugin)
    }
}
