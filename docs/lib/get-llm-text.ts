import type { InferPageType } from "fumadocs-core/source";
import type { source } from "@/lib/source";
import { createElement } from "react";
import { getMDXComponents } from "@/mdx-components";

/**
 * HTMLをLLM向けのプレーンテキスト（Markdown風）に変換する
 */
function htmlToText(html: string): string {
	let text = html;

	// <br> → 改行
	text = text.replace(/<br\s*\/?>/gi, "\n");

	// 見出し
	text = text.replace(/<h1[^>]*>(.*?)<\/h1>/gi, "# $1\n");
	text = text.replace(/<h2[^>]*>(.*?)<\/h2>/gi, "## $1\n");
	text = text.replace(/<h3[^>]*>(.*?)<\/h3>/gi, "### $1\n");
	text = text.replace(/<h4[^>]*>(.*?)<\/h4>/gi, "#### $1\n");

	// テーブルをMarkdown形式に変換
	text = text.replace(/<table[^>]*>([\s\S]*?)<\/table>/gi, (_match, tableContent: string) => {
		const rows: string[][] = [];
		const rowRegex = /<tr[^>]*>([\s\S]*?)<\/tr>/gi;
		let rowMatch: RegExpExecArray | null;
		while ((rowMatch = rowRegex.exec(tableContent)) !== null) {
			const cells: string[] = [];
			const cellRegex = /<(?:td|th)[^>]*>([\s\S]*?)<\/(?:td|th)>/gi;
			let cellMatch: RegExpExecArray | null;
			while ((cellMatch = cellRegex.exec(rowMatch[1])) !== null) {
				cells.push(cellMatch[1].replace(/<[^>]*>/g, "").trim());
			}
			if (cells.length > 0) rows.push(cells);
		}
		if (rows.length === 0) return "";

		const lines: string[] = [];
		lines.push("| " + rows[0].join(" | ") + " |");
		lines.push("| " + rows[0].map(() => "---").join(" | ") + " |");
		for (let i = 1; i < rows.length; i++) {
			lines.push("| " + rows[i].join(" | ") + " |");
		}
		return "\n" + lines.join("\n") + "\n";
	});

	// コードブロック（pre > code）
	text = text.replace(/<pre[^>]*><code[^>]*>([\s\S]*?)<\/code><\/pre>/gi, (_match, code: string) => {
		const decoded = code.replace(/<[^>]*>/g, "")
			.replace(/&lt;/g, "<")
			.replace(/&gt;/g, ">")
			.replace(/&amp;/g, "&")
			.replace(/&quot;/g, '"');
		return "\n```\n" + decoded + "\n```\n";
	});

	// インラインコード
	text = text.replace(/<code[^>]*>(.*?)<\/code>/gi, "`$1`");

	// リスト
	text = text.replace(/<li[^>]*>([\s\S]*?)<\/li>/gi, "- $1\n");
	text = text.replace(/<\/?[ou]l[^>]*>/gi, "\n");

	// 太字・イタリック
	text = text.replace(/<(?:strong|b)[^>]*>(.*?)<\/(?:strong|b)>/gi, "**$1**");
	text = text.replace(/<(?:em|i)[^>]*>(.*?)<\/(?:em|i)>/gi, "*$1*");

	// リンク
	text = text.replace(/<a[^>]*href="([^"]*)"[^>]*>(.*?)<\/a>/gi, "[$2]($1)");

	// 段落
	text = text.replace(/<p[^>]*>([\s\S]*?)<\/p>/gi, "$1\n\n");

	// 残りのHTMLタグを除去
	text = text.replace(/<[^>]*>/g, "");

	// HTMLエンティティをデコード
	text = text.replace(/&lt;/g, "<");
	text = text.replace(/&gt;/g, ">");
	text = text.replace(/&amp;/g, "&");
	text = text.replace(/&quot;/g, '"');
	text = text.replace(/&#39;/g, "'");
	text = text.replace(/&nbsp;/g, " ");

	// 連続する空行を最大2行に制限
	text = text.replace(/\n{4,}/g, "\n\n\n");

	return text.trim();
}

/**
 * processedテキストからimport/export/JSXを除去してクリーンにする
 */
function cleanProcessedText(content: string): string {
	const lines = content.split("\n");
	const cleaned: string[] = [];
	let inExportBlock = false;
	let inCodeBlock = false;

	for (const line of lines) {
		const trimmed = line.trim();

		// フェンスドコードブロックのトグル
		if (trimmed.startsWith("```")) {
			inCodeBlock = !inCodeBlock;
			cleaned.push(line);
			continue;
		}

		// コードブロック内はそのまま保持
		if (inCodeBlock) {
			cleaned.push(line);
			continue;
		}

		// import文をスキップ
		if (trimmed.startsWith("import ")) continue;

		// export const/default ブロック（複数行）をスキップ
		if (trimmed.startsWith("export const ") || trimmed.startsWith("export default ")) {
			inExportBlock = true;
			continue;
		}
		if (inExportBlock) {
			if (trimmed === "];" || trimmed === "};") {
				inExportBlock = false;
			}
			continue;
		}

		cleaned.push(line);
	}

	let result = cleaned.join("\n");

	// 自己閉じJSXタグを除去
	result = result.replace(/<[A-Z][A-Za-z]*\s[^>]*\/>/g, "");
	result = result.replace(/<[A-Z][A-Za-z]*\s*\/>/g, "");

	// CalloutContainer → テキスト
	result = result.replace(/<CalloutContainer[^>]*>\s*/g, "");
	result = result.replace(/<\/CalloutContainer>/g, "");
	result = result.replace(/<CalloutTitle>\s*/g, "**");
	result = result.replace(/\s*<\/CalloutTitle>/g, "**\n");
	result = result.replace(/<CalloutDescription>\s*/g, "");
	result = result.replace(/\s*<\/CalloutDescription>/g, "");

	// details/summary → テキスト
	result = result.replace(/<details>/g, "");
	result = result.replace(/<\/details>/g, "");
	result = result.replace(/<summary>\s*/g, "**");
	result = result.replace(/\s*<\/summary>/g, "**\n");

	// 残りのJSXタグを除去
	result = result.replace(/<[A-Z][A-Za-z]*[^>]*>/g, "");
	result = result.replace(/<\/[A-Z][A-Za-z]*>/g, "");

	// 連続する空行を制限
	result = result.replace(/\n{4,}/g, "\n\n\n");

	return result.trim();
}

export async function getLLMText(page: InferPageType<typeof source>) {
	try {
		const { renderToStaticMarkup } = await import("react-dom/server");
		const MDX = page.data.body;
		const html = renderToStaticMarkup(
			createElement(MDX, { components: getMDXComponents() })
		);
		const text = htmlToText(html);
		return `# ${page.data.title} (${page.url})\n\n${text}`;
	} catch {
		// レンダリングに失敗した場合はprocessedテキストをクリーンアップして返す
		const processed = await page.data.getText("processed");
		const cleaned = cleanProcessedText(processed);
		return `# ${page.data.title} (${page.url})\n\n${cleaned}`;
	}
}
