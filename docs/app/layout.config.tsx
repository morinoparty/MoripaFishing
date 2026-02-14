import type { BaseLayoutProps } from "@/components/layout/shared";

export function baseOptions(): BaseLayoutProps {
    return {
        nav: {
            title: (
                <div className="flex items-center gap-2">
                    <span className="text-lg font-bold">MoripaFishing</span>
                </div>
            ),
            transparentMode: "top",
        },
        themeSwitch: {
            enabled: true,
            mode: "light-dark",
        },
        githubUrl: "https://github.com/morinoparty/MoripaFishing",
        modrinthUrl: "https://modrinth.com/plugin/moripafishing",
    };
}
