package party.morino.moripafishing.api.core.world.spi

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * `WeatherPolicy.External` で選択される外部天候ソース。
 *
 * `ServicesManager` に登録する。登録はプラグインの無効化時に自動で解除される。
 * 同じ [key] の登録が複数ある場合は優先度の高い登録が使われる。
 *
 * 型の制約: シグネチャには `:api` の型・`Key`・`String`・Java のプリミティブ・`java.util` のコレクションのみを使う。
 * 実装側も `:api` のクラスのコンストラクタを既定引数を省略して呼ばないこと (詳細は [WorldState])。
 * スレッド: [getWeather] は任意のスレッドから呼ばれる。
 */
interface WeatherSource {
    /**
     * このソースを識別するキー。`WeatherPolicy.External.source` と照合される。
     */
    val key: Key

    /**
     * `true` の場合、このソースの天候をコアが `WorldState.weather` として適用させる。
     * `false` の場合、天候は魚の条件にのみ使い、ワールドの天候は変更させない。
     */
    val appliesToWorld: Boolean

    /**
     * 現在の天候を返す。任意のスレッドから呼ばれるため、Bukkit API を直接呼ばないこと。
     *
     * @param worldId 釣りワールドの ID
     * @param worldKey 対応する Bukkit ワールドのキー
     * @return 現在の天候
     */
    fun getWeather(
        worldId: FishingWorldId,
        worldKey: Key,
    ): WeatherType
}
