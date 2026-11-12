package party.morino.moripafishing.core.internationalization

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.api.core.fish.FishManager
import party.morino.moripafishing.api.core.rarity.RarityManager
import party.morino.moripafishing.api.core.world.WorldManager
import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.Properties
import java.util.logging.Logger

/**
 * 翻訳 (MiniMessage) を `GlobalTranslator` に登録する。
 *
 * 翻訳ディレクトリの `<locale>.properties` をすべて読み込み、同梱の既定値に上書きして登録する。
 * 続けて魚・レアリティ・ワールドの表示名を登録する。properties に同じキーがある場合は properties を優先する。
 *
 * @property directory 翻訳ファイルを置くディレクトリ
 * @property config 既定ロケールの参照先
 * @property fishes 魚の表示名の参照先
 * @property rarities レアリティの表示名の参照先
 * @property worlds ワールドの表示名の参照先
 * @property logger 不正な翻訳ファイルの警告の出力先
 */
internal class Translations(
    private val directory: File,
    private val config: ConfigManager,
    private val fishes: FishManager,
    private val rarities: RarityManager,
    private val worlds: WorldManager,
    private val logger: Logger,
) {
    private var store: MiniMessageTranslationStore? = null

    /**
     * 翻訳を読み込み、`GlobalTranslator` に登録する。登録済みの翻訳があれば差し替える。
     *
     * 新しいストアを作り終えてから差し替えるため、途中で例外が発生した場合は以前の翻訳が残る。
     */
    fun load() {
        val next = MiniMessageTranslationStore.create(Key.key("moripafishing", "translations"))
        next.defaultLocale(config.getConfig().defaultLocale)
        copyBundled()
        loadProperties(next)
        loadDataNames(next)
        close()
        GlobalTranslator.translator().addSource(next)
        store = next
    }

    /**
     * `GlobalTranslator` から自分のストアを外す。未登録なら何もしない。
     */
    fun close() {
        store?.let { GlobalTranslator.translator().removeSource(it) }
        store = null
    }

    /** 同梱の翻訳ファイルのうち、翻訳ディレクトリに無いものをコピーする。 */
    private fun copyBundled() {
        BUNDLED.forEach { name ->
            val file = directory.resolve("$name.properties")
            if (file.exists()) return@forEach
            bundled(name)?.use { input -> file.outputStream().use { input.copyTo(it) } }
        }
    }

    /**
     * `<language>.properties` または `<language>_<COUNTRY>.properties` を読み込む。
     *
     * 次のファイルは警告してスキップする。
     * - 名前がこの形に合わないファイル (`en_us.properties` や `messages.properties` など)
     * - 既に読み込んだファイルと同じロケールになるファイル (`iw` と `he` など)
     * - properties として不正なファイル (不正な `\uXXXX` エスケープなど)
     */
    private fun loadProperties(store: MiniMessageTranslationStore) {
        val loaded = mutableSetOf<Locale>()
        directory
            .listFiles { file -> file.isFile && file.extension == "properties" }
            .orEmpty()
            .sortedBy { it.name }
            .forEach { file ->
                val problem = loadProperties(store, file, loaded)
                if (problem != null) logger.warning("Skipping translation file ${file.name}: $problem")
            }
    }

    /** 1 ファイルを読み込んで登録する。スキップした場合はその理由を返す。 */
    private fun loadProperties(
        store: MiniMessageTranslationStore,
        file: File,
        loaded: MutableSet<Locale>,
    ): String? {
        val name = file.nameWithoutExtension
        if (!LOCALE_NAME.matches(name)) return "the name must be like ja_JP or en"
        val locale = checkNotNull(Translator.parseLocale(name))
        if (locale in loaded) return "locale $locale is already loaded from another file"
        val properties = Properties()
        try {
            bundled(name)?.use { properties.load(it.reader(Charsets.UTF_8)) }
            file.reader(Charsets.UTF_8).use { properties.load(it) }
        } catch (e: IllegalArgumentException) {
            return e.message
        }
        loaded += locale
        properties.forEach { (key, value) -> store.register(key.toString(), locale, value.toString()) }
        return null
    }

    private fun loadDataNames(store: MiniMessageTranslationStore) {
        fishes.getAll().forEach { store.registerAbsent(it.id.toTranslateKey(), it.displayName) }
        rarities.getAll().forEach { store.registerAbsent(it.id.toTranslateKey(), it.displayName) }
        worlds.getWorlds().forEach { store.registerAbsent(it.getId().toTranslateKey(), it.getConfig().name) }
    }

    /** properties で登録済みのキー・ロケールを除いて登録する。 */
    private fun MiniMessageTranslationStore.registerAbsent(
        key: String,
        names: Map<Locale, String>,
    ) {
        names.forEach { (locale, name) -> if (!contains(key, locale)) register(key, locale, name) }
    }

    private fun bundled(name: String): InputStream? = Translations::class.java.getResourceAsStream("/translation/$name.properties")

    private companion object {
        /** プラグインに同梱している翻訳ファイルの名前。 */
        val BUNDLED = listOf("ja_JP", "en_US")

        /** 翻訳ファイル名として受け付けるロケール名。 */
        val LOCALE_NAME = Regex("[a-z]{2,3}(_[A-Z]{2})?")
    }
}
