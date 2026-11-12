package party.morino.moripafishing.core.world

import java.util.logging.Handler
import java.util.logging.LogRecord

/** 警告ログのメッセージを記録する。 */
internal class WarningRecorder : Handler() {
    val messages = mutableListOf<String>()

    override fun publish(record: LogRecord) {
        messages += record.message
    }

    override fun flush() = Unit

    override fun close() = Unit
}
