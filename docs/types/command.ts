/**
 * コマンドのステータスを表す型
 */
export type CommandStatus = "stable" | "newly" | "beta" | "proposal" | "deprecated";

/**
 * コマンドの情報を表すインターフェース
 */
export interface Command {
    /**
     * コマンド名
     */
    command: string;

    /**
     * コマンドの説明
     */
    description: string;

    /**
     * コマンドのタグ
     */
    tags: string[];

    /**
     * コマンドのエイリアス
     */
    aliases: string[];

    /**
     * コマンドのパーミッション
     */
    permission: string;

    /**
     * コマンドのステータス
     */
    status: CommandStatus;
}
