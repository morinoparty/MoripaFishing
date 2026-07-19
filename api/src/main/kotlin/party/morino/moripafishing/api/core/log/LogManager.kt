package party.morino.moripafishing.api.core.log

/**
 * ログを管理するインターフェース
 */
interface LogManager {
    /**
     * INFOレベルのログを出力する
     * @param message ログメッセージ
     */
    fun info(message: String)

    /**
     * WARNINGレベルのログを出力する
     * @param message ログメッセージ
     */
    fun warning(message: String)

    /**
     * SEVEREレベルのログを出力する
     * @param message ログメッセージ
     */
    fun severe(message: String)
}
