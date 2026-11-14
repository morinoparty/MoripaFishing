package party.morino.moripafishing.core.world

import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.HandlerList
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.config.climate.ClimateConfig
import party.morino.moripafishing.api.config.world.FishingWorldConfig
import party.morino.moripafishing.api.config.world.WorldBinding
import party.morino.moripafishing.api.core.world.FishingWorld
import party.morino.moripafishing.api.core.world.WorldManager
import party.morino.moripafishing.api.event.world.FishingWorldCreateEvent
import party.morino.moripafishing.api.event.world.FishingWorldDeleteEvent
import party.morino.moripafishing.api.event.world.FishingWorldDeletedEvent
import party.morino.moripafishing.api.event.world.FishingWorldLoadEvent
import party.morino.moripafishing.api.event.world.FishingWorldUnloadEvent
import party.morino.moripafishing.api.event.world.FishingWorldWeatherChangeEvent
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType
import party.morino.moripafishing.core.internationalization.DisplayNameRegistry
import party.morino.moripafishing.core.world.weather.VanillaWeatherCache
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * [WorldManager] のコア実装。
 *
 * 釣りワールドの定義 (設定ファイル) を保持し、各ワールドのあるべき状態を決めて Integration に適用させる。
 * コア自身は Bukkit ワールドを一切変更せず、`ServicesManager` に登録された
 * `WorldStateApplier` / `WorldLifecycle` / `WeatherSource` を呼ぶだけである。
 * Integration が未導入でも、定義の読み込み・天候の決定 (魚の抽選条件) は動作する。
 *
 * 外部ワールド (`WorldBinding.External` で登録した既存ワールド) は読み取り専用で、
 * 状態の適用・作成・アンロードを一切行わない。天候の決定とイベントの発火のみ行う。
 *
 * ライフサイクル:
 * 1. [enable] (プラグイン有効化時): 定義を読み込み、Listener を登録する。ワールドには触れない。
 * 2. [start] (`ServerLoadEvent`。全 Integration の登録後): ロード済みワールドへ状態を適用し、
 *    未ロードのワールドを `WorldLifecycle` で作成し、リフレッシュループを開始する。
 * 3. [disable] (プラグイン無効化時): ループと Listener を止める。ワールドには触れない。
 *
 * [start] 前は定義の読み書きのみ行い、Integration への適用・ワールドの作成は行わない
 * ([createWorld] は失敗し、[updateWorldConfig] は保存のみ行う)。
 *
 * @property plugin コアプラグイン
 * @property configManager `config.json` の参照先
 * @property store 釣りワールド設定ファイルの読み書き
 * @property names ワールドの表示名の登録先。定義の追加・変更・削除のたびに同期する
 * @property clock 現在時刻の取得元 (テストで差し替える)
 */
@Suppress("TooManyFunctions") // WorldManager (公開 API) と WorldContext (Listener 向け) の実装を 1 か所で状態ごと管理するため
internal class WorldManagerImpl(
    private val plugin: Plugin,
    private val configManager: ConfigManager,
    private val store: WorldConfigStore,
    private val names: DisplayNameRegistry,
    private val clock: Clock = Clock.systemUTC(),
) : WorldManager,
    WorldContext {
    private val worlds = ConcurrentHashMap<FishingWorldId, FishingWorldImpl>()
    private val services = WorldServices(plugin)
    private val vanillaWeather = VanillaWeatherCache()
    private val listener = WorldEventListener(this)
    private var refreshTask: BukkitTask? = null
    private var started = false

    /**
     * 定義を読み込み、Listener を登録する。プラグイン有効化時に呼ぶ。ワールドには触れない。
     */
    fun enable() {
        withoutKeyConflicts(store.loadAll()).forEach { define(it) }
        plugin.server.pluginManager.registerEvents(listener, plugin)
        plugin.server.pluginManager.registerEvents(vanillaWeather, plugin)
        vanillaWeather.primeAll(plugin.server.worlds)
        plugin.logger.info("Defined fishing worlds: ${worlds.keys.map { it.value }.sorted()}")
    }

    /**
     * 釣りワールドの運用を始める。`ServerLoadEvent` から呼ばれ、2 回目以降は何もしない。メインスレッド。
     */
    fun start() {
        checkMainThread("start")
        if (started) return
        started = true
        scheduleRefresh()
        worlds.values.forEach(::activate)
        refresh()
    }

    /**
     * ループと Listener を止め、定義を破棄する。プラグイン無効化時に呼ぶ。ワールドには触れない。
     */
    fun disable() {
        refreshTask?.cancel()
        refreshTask = null
        HandlerList.unregisterAll(listener)
        HandlerList.unregisterAll(vanillaWeather)
        worlds.clear()
        started = false
    }

    /**
     * 全ワールドの天候を観測して変化を通知し、ロード済みワールドへ状態を適用させる。メインスレッド。
     */
    fun refresh() {
        checkMainThread("refresh")
        val now = now()
        worlds.values.forEach { world ->
            val decision = world.decideWeather(now)
            val previous = world.observedWeather
            if (previous != null && previous != decision.weather) {
                FishingWorldWeatherChangeEvent(world.getId(), previous, decision.weather).callEvent()
            }
            world.observedWeather = decision.weather
            if (isLoaded(world)) world.desiredState(now, decision)?.let(services::apply)
        }
    }

    /**
     * Bukkit ワールドのロードに追従する。[start] 前は何もしない ([start] がまとめて処理する)。
     */
    fun onBukkitWorldLoaded(world: World) {
        if (!started) return
        announceLoaded(worldOf(world.key) ?: return)
    }

    /**
     * Bukkit ワールドのアンロードに追従する。
     */
    fun onBukkitWorldUnloaded(world: World) {
        val fishingWorld = worldOf(world.key) ?: return
        FishingWorldUnloadEvent(fishingWorld.getId()).callEvent()
    }

    override fun getDefaultWorldId(): FishingWorldId = configManager.getConfig().world.defaultId

    override fun getWorlds(): List<FishingWorld> = worlds.values.sortedBy { it.getId().value }

    override fun getWorld(id: FishingWorldId): FishingWorld? = worlds[id]

    override fun getWorldByKey(worldKey: Key): FishingWorld? = worldOf(worldKey)

    override fun getGeneratorIds(): Set<String> = services.lifecycle()?.getGeneratorIds() ?: emptySet()

    override fun createWorld(
        id: FishingWorldId,
        generatorId: String,
    ): Boolean {
        checkMainThread("createWorld")
        val rejection = creationRejection(id)
        if (rejection != null) {
            plugin.logger.warning("Cannot create fishing world ${id.value}: $rejection")
            return false
        }
        // 作成中に発火する WorldLoadEvent で見つけられるよう、先に定義する
        val binding = WorldBinding.Managed(generator = generatorId)
        val config =
            FishingWorldConfig(
                id = id,
                name = mapOf(configManager.getConfig().defaultLocale to id.value),
                world = binding,
            )
        store.save(config)
        val created = tryCreate(define(config), binding)
        if (created) {
            FishingWorldCreateEvent(id).callEvent()
        } else {
            forget(id)
            store.delete(id)
        }
        return created
    }

    @Suppress("ReturnCount") // 削除を中止する理由 (未定義・アンロード不可・イベントのキャンセル) ごとに早期 return するため
    override fun deleteWorld(id: FishingWorldId): Boolean {
        checkMainThread("deleteWorld")
        val world = worlds[id] ?: return false
        // 外部ワールドは登録を外すだけで、Bukkit ワールドはアンロードしない
        val loaded = managed(world) != null && isLoaded(world)
        val lifecycle = services.lifecycle()
        if (loaded && lifecycle == null) {
            plugin.logger.warning(
                "Cannot delete fishing world ${id.value}: it is loaded and no WorldLifecycle is registered",
            )
            return false
        }
        if (!FishingWorldDeleteEvent(id).callEvent()) return false
        if (loaded && lifecycle?.unloadWorld(world.getWorldKey()) != true) {
            plugin.logger.warning("Cannot delete fishing world ${id.value}: the Bukkit world could not be unloaded")
            return false
        }
        forget(id)
        store.delete(id)
        FishingWorldDeletedEvent(id).callEvent()
        return true
    }

    /**
     * 設定を保存し、運用開始後かつロード済みなら即座に適用させる。
     * `generator` は作成時のみ参照されるため、既存ワールドで変更しても効果はない。
     */
    override fun updateWorldConfig(config: FishingWorldConfig): Boolean {
        checkMainThread("updateWorldConfig")
        val world = worlds[config.id] ?: return false
        require(WorldKeys.of(config) == world.getWorldKey()) {
            "the bukkit world of fishing world ${config.id.value} cannot be changed by updateWorldConfig"
        }
        if (world.getConfig() != config) {
            store.save(config)
            replaceConfig(world, config)
            if (started && isLoaded(world)) push(world)
        }
        return true
    }

    /**
     * 設定ファイルを読み直す。消えたワールドは定義から外すのみで、アンロードもイベントの発火も行わない。
     * 運用開始後 ([start] 後) は、リフレッシュ間隔を更新し、未ロードのワールドの作成を再試行し、
     * 追加されたロード済みワールドへ状態を適用してから全ワールドをリフレッシュする。
     */
    override fun reload() {
        checkMainThread("reload")
        val definitions = withoutKeyConflicts(store.loadAll()).associateBy { it.id }
        (worlds.keys - definitions.keys).forEach(::forget)
        val added = mutableListOf<FishingWorldImpl>()
        definitions.values.forEach { config ->
            val existing = worlds[config.id]
            // 対応する Bukkit ワールドが変わった定義は新しいワールドとして扱う
            if (existing != null && existing.getWorldKey() == WorldKeys.of(config)) {
                replaceConfig(existing, config)
            } else {
                added += define(config)
            }
        }
        if (!started) return
        scheduleRefresh()
        worlds.values.forEach { world ->
            when {
                isLoaded(world) -> if (world in added) announceLoaded(world)
                else -> managed(world)?.let { tryCreate(world, it) }
            }
        }
        refresh()
    }

    override fun now(): ZonedDateTime {
        val zone = ZoneId.of(configManager.getConfig().world.timeZone)
        return ZonedDateTime.now(clock.withZone(zone))
    }

    override fun defaultClimate(): ClimateConfig = configManager.getConfig().world.defaultClimate

    override fun vanillaWeather(worldKey: Key): WeatherType = vanillaWeather.get(worldKey)

    override fun externalWeather(
        source: Key,
        worldId: FishingWorldId,
        worldKey: Key,
    ): WeatherDecision? = services.externalWeather(source, worldId, worldKey)

    private fun define(config: FishingWorldConfig): FishingWorldImpl {
        val world = FishingWorldImpl(config, this)
        worlds[config.id] = world
        names.register(config.id.toTranslateKey(), config.name)
        return world
    }

    private fun replaceConfig(
        world: FishingWorldImpl,
        config: FishingWorldConfig,
    ) {
        world.replaceConfig(config)
        names.register(config.id.toTranslateKey(), config.name)
    }

    /** 定義から外し、表示名も外す。 */
    private fun forget(id: FishingWorldId) {
        worlds.remove(id)
        names.unregister(id.toTranslateKey())
    }

    /**
     * ロード済みなら状態を適用して [FishingWorldLoadEvent] を発火し、未ロードなら作成を試みる。
     * 未ロードの外部ワールドは作成せず、ロードされるのを待つ。
     */
    private fun activate(world: FishingWorldImpl) {
        when {
            isLoaded(world) -> announceLoaded(world)
            else -> {
                val binding = managed(world)
                if (binding != null) {
                    tryCreate(world, binding)
                } else {
                    plugin.logger.info(
                        "External fishing world ${world.getId().value} is waiting for ${world.getWorldKey().asString()} to load",
                    )
                }
            }
        }
    }

    private fun announceLoaded(world: FishingWorldImpl) {
        push(world)
        FishingWorldLoadEvent(world.getId()).callEvent()
    }

    /**
     * 定義済みワールドの Bukkit ワールドを `WorldLifecycle` で作成する。失敗した場合は理由を警告して `false` を返す。
     * 成功時の適用と [FishingWorldLoadEvent] は、作成中に発火する `WorldLoadEvent` 経由で行われる。
     */
    private fun tryCreate(
        world: FishingWorldImpl,
        binding: WorldBinding.Managed,
    ): Boolean {
        val generator = binding.generator
        val lifecycle = services.lifecycle()
        val failure =
            when {
                lifecycle == null -> "no WorldLifecycle is registered"
                generator !in lifecycle.getGeneratorIds() -> "unknown generator '$generator'"
                !lifecycle.createWorld(world.getWorldKey(), generator) -> "the WorldLifecycle failed to create it"
                else -> null
            }
        failure?.let {
            plugin.logger.warning("Cannot create the Bukkit world of fishing world ${world.getId().value}: $it")
        }
        return failure == null
    }

    /** [createWorld] で新しい定義を作れない理由。作れる場合は `null`。 */
    private fun creationRejection(id: FishingWorldId): String? {
        val key = runCatching { WorldKeys.of(id) }.getOrNull()
        return when {
            !started -> "fishing worlds are not started yet (wait until the server has loaded)"
            worlds.containsKey(id) -> "it is already defined"
            key == null -> "'${id.value}' is not a valid world key value"
            plugin.server.getWorld(key) != null -> "Bukkit world ${key.asString()} is already loaded"
            else -> null
        }
    }

    private fun scheduleRefresh() {
        refreshTask?.cancel()
        val interval = configManager.getConfig().world.refreshIntervalSeconds * TICKS_PER_SECOND
        refreshTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable { refresh() }, interval, interval)
    }

    /** 管理ワールドなら状態を適用させる。外部ワールドには何もしない。 */
    private fun push(world: FishingWorldImpl) {
        world.desiredState(now())?.let(services::apply)
    }

    /**
     * Bukkit ワールドのキーが重複する定義を除く。重複した場合は先に読み込んだ定義 (ID 順) を残し、残りを警告して捨てる。
     */
    private fun withoutKeyConflicts(configs: List<FishingWorldConfig>): List<FishingWorldConfig> {
        val owners = HashMap<Key, FishingWorldId>()
        return configs.filter { config ->
            val key = WorldKeys.of(config)
            val owner = owners.putIfAbsent(key, config.id)
            if (owner != null) {
                plugin.logger.warning(
                    "Skipping fishing world ${config.id.value}: Bukkit world ${key.asString()} is already bound to ${owner.value}",
                )
            }
            owner == null
        }
    }

    /** 管理ワールドなら、その設定。外部ワールドは `null`。 */
    private fun managed(world: FishingWorldImpl): WorldBinding.Managed? = world.getConfig().world as? WorldBinding.Managed

    private fun isLoaded(world: FishingWorld): Boolean = plugin.server.getWorld(world.getWorldKey()) != null

    private fun worldOf(worldKey: Key): FishingWorldImpl? {
        val normalized = WorldKeys.normalize(worldKey)
        return worlds.values.firstOrNull { it.getWorldKey() == normalized }
    }

    private fun checkMainThread(operation: String) {
        check(Bukkit.isPrimaryThread()) { "WorldManager.$operation must be called on the main thread" }
    }

    private companion object {
        const val TICKS_PER_SECOND = 20L
    }
}
