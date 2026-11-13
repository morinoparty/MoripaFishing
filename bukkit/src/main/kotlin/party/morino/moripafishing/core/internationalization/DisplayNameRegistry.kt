package party.morino.moripafishing.core.internationalization

import java.util.Locale

/**
 * 定義 (魚・レアリティ・ワールド) の表示名を翻訳キーとして登録する窓口。
 *
 * 定義を管理するマネージャーが、定義の追加・置き換え・削除のたびに呼ぶ。任意のスレッドから呼べる。
 */
internal interface DisplayNameRegistry {
    /**
     * [key] の表示名を [names] で置き換える。同じ表示名が登録済みなら何もしない。
     *
     * @param key 翻訳キー
     * @param names ロケールごとの表示名 (MiniMessage)
     */
    fun register(
        key: String,
        names: Map<Locale, String>,
    )

    /**
     * [key] の表示名を外す。未登録なら何もしない。
     *
     * @param key 翻訳キー
     */
    fun unregister(key: String)
}
