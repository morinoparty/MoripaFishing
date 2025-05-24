package party.morino.moripafishing.core.fishing

import org.koin.core.component.KoinComponent
import party.morino.moripafishing.api.core.angler.Angler
import party.morino.moripafishing.api.core.fishing.ApplyValue
import party.morino.moripafishing.api.core.fishing.WaitTimeManager
import party.morino.moripafishing.api.model.angler.AnglerId
import party.morino.moripafishing.api.model.world.FishingWorldId
import party.morino.moripafishing.api.model.world.Spot
import java.time.Duration
import java.time.ZonedDateTime

/**
 * WaitTimeManagerの実装クラス
 *
 * 現状は単純なメモリ管理。今後永続化や詳細なロジックを追加する場合はここを拡張する。
 */

class WaitTimeManagerImpl : WaitTimeManager, KoinComponent {
    // 各種適用値を保持するマップ
    private val spotValues: MutableList<Triple<Spot, ApplyValue, ZonedDateTime?>> = mutableListOf()
    private val anglerValues: MutableList<Triple<AnglerId, ApplyValue, ZonedDateTime?>> = mutableListOf()
    private val worldValues: MutableList<Triple<FishingWorldId, ApplyValue, ZonedDateTime?>> = mutableListOf()

    /**
     * Spot単位で適用値を設定
     * @param spot スポット
     * @param applyValue 適用値
     * @param limit 有効期限(ミリ秒)
     */
    override fun applyForSpot(
            spot: Spot,
            applyValue: ApplyValue,
            limit: Long?,
    ) {
        val limitDateTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        spotValues.add(Triple(spot, applyValue, limitDateTime))
    }

    /**
     * Angler単位で適用値を設定
     */
    override fun applyForAngler(
            anglerId: AnglerId,
            applyValue: ApplyValue,
            limit: Long?,
    ) {
        val limitDateTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        anglerValues.add(Triple(anglerId, applyValue, limitDateTime))
    }

    /**
     * World単位で適用値を設定
     */
    override fun applyForWorld(
            worldId: FishingWorldId,
            applyValue: ApplyValue,
            limit: Long?,
    ) {
        val limitDateTime = limit?.let { ZonedDateTime.now().plus(Duration.ofMillis(it)) }
        worldValues.add(Triple(worldId, applyValue, limitDateTime))
    }

    /**
     * 釣り人に対する待機時間を取得
     * 現状はデフォルト値(100, 600)を返す。今後は各種適用値を合成して返すよう拡張可能。
     */
    override fun getWaitTime(angler: Angler): Pair<Int, Int> {
        // TODO: spot, angler, worldの値を合成して返すロジックを実装
        return 1 to 1
    }
}
