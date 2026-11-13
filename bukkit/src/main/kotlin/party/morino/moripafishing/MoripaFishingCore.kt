package party.morino.moripafishing

import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import party.morino.moripafishing.api.MoripaFishingAPI
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.angler.AnglerManager
import party.morino.moripafishing.api.core.fish.FishDrawer
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.event.MoripaFishingReloadedEvent
import party.morino.moripafishing.config.ConfigManagerImpl
import party.morino.moripafishing.config.PluginPaths
import party.morino.moripafishing.core.angler.AnglerManagerImpl
import party.morino.moripafishing.core.fish.FishDrawerImpl
import party.morino.moripafishing.core.fish.FishItemFactory
import party.morino.moripafishing.core.fish.FishManagerImpl
import party.morino.moripafishing.core.internationalization.Translations
import party.morino.moripafishing.core.rarity.RarityManagerImpl
import party.morino.moripafishing.core.world.WorldConfigStore
import party.morino.moripafishing.core.world.WorldManagerImpl
import party.morino.moripafishing.listener.PlayerFishingListener
import java.util.logging.Logger

/**
 * MoripaFishing のコンポジションルート。全コンポーネントを組み立て、有効化・再読み込み・無効化を順に行う。
 *
 * @property plugin プラグイン本体
 */
internal class MoripaFishingCore(
    private val plugin: MoripaFishing,
) : MoripaFishingAPI {
    private val paths = PluginPaths(plugin.dataFolder)
    private val config = ConfigManagerImpl(paths.config)
    private val worlds = WorldManagerImpl(plugin, config, WorldConfigStore(paths.world, plugin.logger))
    private val translations = Translations(paths.translation, config, worlds, plugin.logger)
    private val rarities = RarityManagerImpl(paths.rarity, translations, plugin.logger)
    private val fishes = FishManagerImpl(paths.fish, rarities, translations, plugin.logger)
    private val anglers = AnglerManagerImpl(worlds)
    private val drawer = FishDrawerImpl(fishes, rarities, worlds)
    private val items = FishItemFactory(plugin, config)

    /** プラグインのロガー。 */
    val logger: Logger get() = plugin.logger

    /**
     * 設定と定義を読み込み、リスナーと API を登録する。`config.json` が不正な場合は例外を投げ、有効化は失敗する。
     */
    fun enable() {
        paths.createDirectories()
        config.reload()
        rarities.reload()
        fishes.reload()
        worlds.enable()
        translations.load()
        plugin.server.pluginManager.registerEvents(
            PlayerFishingListener(anglers, drawer, fishes, items, plugin.logger),
            plugin,
        )
        plugin.server.servicesManager.register(MoripaFishingAPI::class.java, this, plugin, ServicePriority.Normal)
        plugin.logger.info("MoripaFishing enabled")
    }

    /**
     * 設定・ワールド・レアリティ・魚・翻訳を読み直し、[MoripaFishingReloadedEvent] を発火する。メインスレッドから呼ぶこと。
     *
     * `config.json` の読み込みに失敗した場合は例外を投げ、以前の設定のまま以降の処理を行わない。
     */
    fun reload() {
        check(Bukkit.isPrimaryThread()) { "reload must be called from the main thread" }
        config.reload()
        worlds.reload()
        rarities.reload()
        fishes.reload()
        translations.load()
        MoripaFishingReloadedEvent().callEvent()
    }

    /**
     * API の登録を解除し、翻訳とワールド管理を止める。
     */
    fun disable() {
        plugin.server.servicesManager.unregisterAll(plugin)
        translations.close()
        worlds.disable()
    }

    override fun getConfigManager(): ConfigManager = config

    override fun getFishManager(): FishManager = fishes

    override fun getRarityManager(): RarityManager = rarities

    override fun getFishDrawer(): FishDrawer = drawer

    override fun getWorldManager(): WorldManager = worlds

    override fun getAnglerManager(): AnglerManager = anglers
}
