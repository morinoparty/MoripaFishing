package party.morino.moripafishing.core.internationalization

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import party.morino.moripafishing.api.config.ConfigManager
import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.Properties
import java.util.logging.Logger

/**
 * 翻訳 (MiniMessage) を `GlobalTranslator` に登録する。
 *
 * 翻訳ディレクトリの `<locale>.properties` をすべて読み込み、同梱の既定値に上書きして登録する。
 * 続けて魚・レアリティ・ワールドの表示名を登録する。properties に同じキー・ロケールがある場合は properties を優先する。
 *
 * 魚・レアリティ・ワールドの表示名は、各マネージャーが [DisplayNameRegistry] として登録・削除する。
 * [load] 前に登録された表示名も保持し、[load] で反映する。
 *
 * @property directory 翻訳ファイルを置くディレクトリ
 * @property config 既定ロケールの参照先
 * @property logger 不正な翻訳ファイルの警告の出力先
 */
internal class Translations(
    private val directory: File,
    private val config: ConfigManager,
    private val logger: Logger,
) : DisplayNameRegistry {
    private var store: MiniMessageTranslationStore? = null

    /** 最後に読み込んだ properties の翻訳 (キー → ロケール → 値)。 */
    private var properties: Map<String, Map<Locale, String>> = emptyMap()

    /** マネージャーから登録された魚・レアリティ・ワールドの表示名 (キー → ロケール → 表示名)。 */
    private val dataNames = HashMap<String, Map<Locale, String>>()

    /**
     * 翻訳を読み込み、`GlobalTranslator` に登録する。登録済みの翻訳があれば差し替える。
     *
     * 新しいストアを作り終えてから差し替えるため、途中で例外が発生した場合は以前の翻訳が残る。
     */
    @Synchronized
    fun load() {
        val next = MiniMessageTranslationStore.create(Key.key("moripafishing", "translations"))
        next.defaultLocale(config.getConfig().defaultLocale)
        copyBundled()
        val nextProperties = loadProperties()
        nextProperties.forEach { (key, values) -> values.forEach { (locale, value) -> next.register(key, locale, value) } }
        dataNames.forEach { (key, names) -> next.registerAbsent(key, names) }
        close()
        GlobalTranslator.translator().addSource(next)
        store = next
        properties = nextProperties
    }

    @Synchronized
    override fun register(
        key: String,
        names: Map<Locale, String>,
    ) {
        // 再読み込みのたびに全定義が登録されるため、変わらない表示名でストアを触らない
        if (dataNames.put(key, names) == names) return
        store?.let { reregister(it, key) }
    }

    @Synchronized
    override fun unregister(key: String) {
        if (dataNames.remove(key) == null) return
        store?.let { reregister(it, key) }
    }

    /**
     * `GlobalTranslator` から自分のストアを外す。未登録なら何もしない。
     */
    @Synchronized
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
     * `<language>.properties` または `<language>_<COUNTRY>.properties` を読み込み、キー → ロケール → 値で返す。
     *
     * 次のファイルは警告してスキップする。
     * - 名前がこの形に合わないファイル (`en_us.properties` や `messages.properties` など)
     * - 既に読み込んだファイルと同じロケールになるファイル (`iw` と `he` など)
     * - properties として不正なファイル (不正な `\uXXXX` エスケープなど)
     */
    private fun loadProperties(): Map<String, Map<Locale, String>> {
        val translations = HashMap<String, MutableMap<Locale, String>>()
        val loaded = mutableSetOf<Locale>()
        directory
            .listFiles { file -> file.isFile && file.extension == "properties" }
            .orEmpty()
            .sortedBy { it.name }
            .forEach { file ->
                val problem = loadProperties(file, translations, loaded)
                if (problem != null) logger.warning("Skipping translation file ${file.name}: $problem")
            }
        return translations
    }

    /** 1 ファイルを読み込んで [translations] に加える。スキップした場合はその理由を返す。 */
    private fun loadProperties(
        file: File,
        translations: MutableMap<String, MutableMap<Locale, String>>,
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
        properties.forEach { (key, value) -> translations.getOrPut(key.toString(), ::HashMap)[locale] = value.toString() }
        return null
    }

    /** [key] をストアから外し、properties の翻訳と表示名を登録し直す。 */
    private fun reregister(
        store: MiniMessageTranslationStore,
        key: String,
    ) {
        store.unregister(key)
        properties[key]?.forEach { (locale, value) -> store.register(key, locale, value) }
        dataNames[key]?.let { store.registerAbsent(key, it) }
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
