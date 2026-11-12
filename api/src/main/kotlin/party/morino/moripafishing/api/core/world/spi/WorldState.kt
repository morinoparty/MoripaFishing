package party.morino.moripafishing.api.core.world.spi

import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.config.world.BorderConfig
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.SpawnPoint
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * コアが決定した、ある釣りワールドのあるべき状態。値の決定はすべてコアが行い、Integration は適用のみ行う。
 *
 * コアは定期リフレッシュ・ワールドロード時・設定変更時に毎回「全項目」を送る。適用は冪等であること。
 * `null` の項目は「変更しない」を意味する。
 *
 * このパッケージ (`WorldState` / [WorldStateApplier] / [WorldLifecycle] / [WeatherSource]) は Integration 向けの SPI である。
 * Integration は `compileOnly(project(":api"))` で参照して `:api` を同梱せず、実装を Bukkit の `ServicesManager` に登録する。
 * 登録はプラグインの無効化時に自動で解除される。
 *
 * ### 型の制約
 * Integration が Kotlin 標準ライブラリを同梱していても安全に呼び出せるよう、このパッケージのシグネチャには
 * `:api` の型・`Key`・`String`・Java のプリミティブとそのボックス型・`java.util` のコレクションのみを使う。
 * Kotlin の関数型・`Pair`・`Result`・value class・inline 関数は使わない。
 * Integration は `:api` のクラスのコンストラクタを既定引数を省略して呼ばないこと。既定引数付きコンストラクタの
 * 合成オーバーロードは `kotlin.jvm.internal.DefaultConstructorMarker` を引数に取り、Integration が同梱した
 * 標準ライブラリのクラスとして解決されるとリンクに失敗する。データクラスの `copy(...)` や既定引数付きの関数は
 * 合成メソッドの引数が `Object` のため影響を受けず、呼んでよい。
 *
 * ### スレッド
 * [WorldStateApplier.apply]・[WorldLifecycle.createWorld]・[WorldLifecycle.unloadWorld] はメインスレッドから呼ばれる。
 * [WorldLifecycle.getGeneratorIds] と [WeatherSource.getWeather] は任意のスレッドから呼ばれる。
 *
 * ### 例外
 * SPI 実装が投げた例外はコアが受け止めて警告し、失敗として扱う (適用は他の実装へ継続、作成・アンロードは `false`、
 * 天候ソースは内蔵カレンダーで代替)。
 *
 * @property worldId 釣りワールドの ID
 * @property worldKey 対応する Bukkit ワールドのキー
 * @property weather 適用する天候。`null` の場合は天候を変更しない (Integration が以前に駆動していた演出だけ解除してよい)
 * @property doWeatherCycle ゲームルール `ADVANCE_WEATHER` の値。[weather] が `null` のとき `true`、それ以外は `false`
 * @property time 適用するワールド時刻 (0..23999)。`null` の場合は時刻を変更しない
 * @property doDaylightCycle ゲームルール `ADVANCE_TIME` の値。[time] が `null` のとき `true`、それ以外は `false`
 * @property border 適用するワールドボーダー。`null` の場合はボーダーを変更しない
 * @property spawn 適用するスポーン地点
 */
data class WorldState(
    val worldId: FishingWorldId,
    val worldKey: Key,
    val weather: WeatherType?,
    val doWeatherCycle: Boolean,
    val time: Long?,
    val doDaylightCycle: Boolean,
    val border: BorderConfig?,
    val spawn: SpawnPoint,
)
