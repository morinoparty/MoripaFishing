import { source } from "../../lib/source";

export const revalidate = false;

export async function GET() {
	const pages = source.getPages();

	const records = pages.map((page) => {
		const title = page.data.title;
		const description = page.data.description || "";
		const url = page.url;
		return `- [${title}](${url}): ${description}`;
	});

	const content = `# MoripaFishing Documentation

> MoripaFishing is a Minecraft fishing plugin with weather system, custom fish, and rarity management.

## Documentation Pages

${records.join("\n")}

## Full Documentation

For complete documentation content, visit: /llms-full.txt
`;

	return new Response(content, {
		headers: {
			"Content-Type": "text/plain; charset=utf-8",
		},
	});
}
