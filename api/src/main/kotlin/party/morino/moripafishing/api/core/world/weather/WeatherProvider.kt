package party.morino.moripafishing.api.core.world.weather

import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.WeatherType

/**
 * 釣りワールドの現在の天候を提供する SPI。
 *
 * MoripaFishing 本体および外部プラグインがこのインターフェースを実装して、
 * 各 `WeatherMode` に対応する天候ソースを差し替え可能にする。
 *
 * pull 型で、呼び出しごとに最新の天候を返す。
 *
 * **スレッド安全性の要件**:
 * `getCurrentWeather` は **メインスレッドおよび非同期スレッドの双方から呼ばれる**。
 * 代表的なホットパスは以下:
 *
 * - `FishingWorldImpl.getCurrentWeather` → Bukkit イベントハンドラ (main)
 * - `FishingWorldImpl.updateState` → プラグインの非同期リフレッシュループ
 * - アドオンからの任意のスレッド (`MoripaFishingAPI` 経由)
 *
 * そのため、実装は以下のいずれかを満たす必要がある:
 *
 * 1. Bukkit API を叩く場合はイベント駆動でスナップショットをキャッシュし、
 *    `getCurrentWeather` はキャッシュのみを参照する
 * 2. 自前の状態のみ参照する (thread-safe なデータ構造を使用)
 * 3. 呼び出し側にメインスレッド限定を要求する (推奨しない、契約違反しやすい)
 */
fun interface WeatherProvider {
    /**
     * 指定された釣りワールドの現在の天候を返す。
     *
     * @param worldId 釣りワールドの ID
     * @return 現在の天候
     */
    fun getCurrentWeather(worldId: FishingWorldId): WeatherType
}
