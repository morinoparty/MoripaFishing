package party.morino.moripafishing.api.config.world

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import party.morino.moripafishing.api.model.world.SpawnPoint
import party.morino.moripafishing.api.serialization.KeySerializer

/**
 * 釣りワールドに対応する Bukkit ワールドと、MoripaFishing がそのワールドを管理するかどうか。JSON では `type` で種類を指定する。
 */
@Serializable
sealed interface WorldBinding {
    /**
     * MoripaFishing が管理するワールド (`moripafishing:<id>`)。
     *
     * Integration を通じて作成・アンロードし、天候・時刻・ボーダー・スポーンを適用させる。
     *
     * @property generator ワールド作成時に使うジェネレーター ID (`WorldLifecycle` が提供する ID)。作成時のみ参照する
     * @property spawn スポーン地点
     * @property border ワールドボーダー。`null` の場合 MoripaFishing はボーダーを管理しない
     */
    @Serializable
    @SerialName("managed")
    data class Managed(
        val generator: String = "void",
        val spawn: SpawnPoint = DEFAULT_SPAWN,
        val border: BorderConfig? = BorderConfig(),
    ) : WorldBinding

    /**
     * 既存の Bukkit ワールドを釣り場として登録した外部ワールド。
     *
     * MoripaFishing はこのワールドを一切変更しない (作成・アンロード・天候・ゲームルール・時刻・スポーン・ボーダー)。
     * テレポート先には Bukkit ワールドのスポーン地点を使う。
     *
     * @property key 登録する Bukkit ワールドのキー (例: `minecraft:overworld`)。`moripafishing` 名前空間は管理ワールド専用のため使えない
     */
    @Serializable
    @SerialName("external")
    data class External(
        @Serializable(with = KeySerializer::class)
        val key: Key,
    ) : WorldBinding {
        init {
            require(key.namespace() != MANAGED_NAMESPACE) {
                "external world key must not use the '$MANAGED_NAMESPACE' namespace: ${key.asString()}"
            }
        }
    }

    companion object {
        /** 管理ワールドの Bukkit ワールドが属する名前空間。 */
        const val MANAGED_NAMESPACE = "moripafishing"
    }
}

/** 既定のスポーン地点 (原点の Y=64、西向き)。 */
private val DEFAULT_SPAWN = SpawnPoint(0.0, 64.0, 0.0, 90f, 0f)
