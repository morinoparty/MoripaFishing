package party.morino.moripafishing.api.config.database

import kotlinx.serialization.Serializable

// データベースに関する設定を保持するデータクラス
@Serializable
data class DatabaseConfig(
    // データベースの種類
    val type: String = "SQLITE",
    // データベースのホスト
    val host: String = "localhost",
    // データベースのポート
    val port: Int = 3306,
    // データベース名
    val database: String = "moripafishing",
    // データベースのユーザー名
    val username: String = "root",
    // データベースのパスワード
    val password: String = "password",
)
