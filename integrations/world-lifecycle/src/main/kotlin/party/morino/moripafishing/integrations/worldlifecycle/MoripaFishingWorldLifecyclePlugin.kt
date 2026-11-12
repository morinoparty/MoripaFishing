package party.morino.moripafishing.integrations.worldlifecycle

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import party.morino.moripafishing.api.core.world.spi.WorldLifecycle
import party.morino.moripafishing.api.core.world.spi.WorldStateApplier
import party.morino.moripafishing.integrations.worldlifecycle.generator.GeneratorRepository
import java.io.File

/**
 * MoripaFishing の WorldLifecycle Integration プラグイン。
 *
 * コア (`MoripaFishing`) に依存してその後にロードされ、`ServicesManager` に次を登録する。
 * 登録は本プラグインの無効化時に自動で解除される。
 *
 * - [BukkitWorldLifecycle] (`WorldLifecycle`): ジェネレーター定義を使った釣りワールドの作成・アンロード
 * - [EnvironmentApplier] (`WorldStateApplier`): 時刻・`DO_DAYLIGHT_CYCLE`・ワールドボーダー・スポーンの適用
 *
 * あわせて、参加時に釣りワールドのスポーンへテレポートする機能 ([JoinTeleportListener]) を提供する。
 */
open class MoripaFishingWorldLifecyclePlugin : JavaPlugin() {
    // 未知キーを無視する寛容な YAML
    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))

    override fun onEnable() {
        val repository = GeneratorRepository(File(dataFolder, "generator"), logger)
        repository.load()
        server.servicesManager.register(
            WorldLifecycle::class.java,
            BukkitWorldLifecycle(repository, logger),
            this,
            ServicePriority.Normal,
        )
        server.servicesManager.register(
            WorldStateApplier::class.java,
            EnvironmentApplier(),
            this,
            ServicePriority.Normal,
        )
        registerJoinTeleport()
        logger.info("MoripaFishing-Integration-WorldLifecycle enabled (generators: ${repository.ids().sorted()}).")
    }

    /**
     * 参加時テレポート機能を設定に応じて登録する。
     */
    private fun registerJoinTeleport() {
        val config = loadJoinTeleportConfig()
        if (config.enabled) server.pluginManager.registerEvents(JoinTeleportListener(config), this)
    }

    private fun loadJoinTeleportConfig(): JoinTeleportConfig {
        dataFolder.mkdirs()
        val file = File(dataFolder, "join-teleport.yml")
        if (!file.exists()) {
            file.writeText(yaml.encodeToString(JoinTeleportConfig.serializer(), JoinTeleportConfig()))
            return JoinTeleportConfig()
        }
        return runCatching {
            yaml.decodeFromString(JoinTeleportConfig.serializer(), file.readText())
        }.getOrElse { error ->
            // 設定が壊れていても Integration 全体の enable を失敗させず、既定値で継続する。
            logger.warning("Failed to parse join-teleport.yml (${error.message}); falling back to defaults.")
            JoinTeleportConfig()
        }
    }
}
