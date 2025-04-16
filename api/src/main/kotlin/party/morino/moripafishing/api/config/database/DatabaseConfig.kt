package party.morino.moripafishing.api.config.database

import kotlinx.serialization.Serializable

// データベースに関する設定を保持するデータクラス
@Serializable
data class DatabaseConfig(
    val type: String = "SQLITE", // データベースの種類
    val host: String = "localhost", // データベースのホスト
    val port: Int = 3306, // データベースのポート
    val database: String = "moripafishing", // データベース名
    val username: String = "root", // データベースのユーザー名
    val password: String = "password" // データベースのパスワード
) 