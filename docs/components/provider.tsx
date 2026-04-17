"use client";
import { RootProvider } from "fumadocs-ui/provider/next";
import type { ReactNode } from "react";
import SearchDialog from "@/components/search";

export function Provider({ children }: { children: ReactNode }) {
    return (
        <RootProvider
            theme={{
                forcedTheme: "light",
                defaultTheme: "light",
                enableSystem: false,
                attribute: ["class", "data-mode"],
            }}
            search={{
                SearchDialog,
            }}
        >
            {children}
        </RootProvider>
    );
}
