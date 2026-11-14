package party.morino.moripafishing.testing

import party.morino.moripafishing.core.internationalization.DisplayNameRegistry
import java.util.Locale

/**
 * 登録された表示名をマップに保持する [DisplayNameRegistry]。
 */
internal class FakeDisplayNameRegistry : DisplayNameRegistry {
    /** 登録中の表示名 (翻訳キー → ロケール → 表示名)。 */
    val names = mutableMapOf<String, Map<Locale, String>>()

    override fun register(
        key: String,
        names: Map<Locale, String>,
    ) {
        this.names[key] = names
    }

    override fun unregister(key: String) {
        names.remove(key)
    }
}
