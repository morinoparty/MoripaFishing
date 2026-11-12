package party.morino.moripafishing.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * Integration の SPI メソッド呼び出しを保護するユーティリティ。
 *
 * SPI のクラスは Integration の jar に同梱されるため、後から SPI に追加したメソッドを
 * 持たない旧版の Integration が導入されていると、呼び出し時に `LinkageError`
 * (`NoSuchMethodError` / `AbstractMethodError`) が発生する。
 * その場合はメソッドごとに一度だけ警告を出し、ワールドを改変せずにフォールバック値を返す。
 */
object IntegrationCall {
    private val warnedMethods: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /**
     * [block] を実行し、`LinkageError` が発生した場合は [fallback] を返す。
     *
     * @param T 戻り値の型
     * @param logger 警告の出力先
     * @param method 警告に表示する SPI メソッド名 (例: `WorldLifecycleProvider.setTime`)
     * @param fallback `LinkageError` 発生時に返す値
     * @param block SPI メソッドの呼び出し
     */
    fun <T> call(
        logger: Logger,
        method: String,
        fallback: T,
        block: () -> T,
    ): T =
        try {
            block()
        } catch (error: LinkageError) {
            if (warnedMethods.add(method)) {
                logger.warning(
                    "$method is not available in the installed integration " +
                        "(${error.javaClass.simpleName}: ${error.message}). " +
                        "Update the integration plugin; this operation is skipped.",
                )
            }
            fallback
        }
}
