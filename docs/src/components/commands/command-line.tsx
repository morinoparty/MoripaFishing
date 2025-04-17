import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@site/src/components/ui/popover";
import {
    Check,
    CircleAlert,
    CircleCheckBig,
    CircleX,
    HandHelping,
} from "lucide-react";
import type React from "react";
import type { Command } from "../../types/command";

interface CommandLineProps {
    command: Command;
}

// Create the badge component
export const CommandLine: React.FC<CommandLineProps> = ({ command }) => {
    // Define the common style
    const commonStyle = "rounded-lg w-full h-[48px] text-black"; // Change text color to black

    // Return the style based on the status
    const getCommandLineStyle = (status: string) => {
        const styles: { [key: string]: string } = {
            stable: `${commonStyle} bg-green-500/50 dark:bg-green-300/70`, // Make green more visible
            newly: `${commonStyle} bg-blue-500/50 dark:bg-blue-300/70`, // Make blue more visible
            beta: `${commonStyle} bg-orange-500/50 dark:bg-orange-200/70`, // Make orange more visible
            proposal: `${commonStyle} bg-gray-500/50 dark:bg-gray-200/70`, // Make gray more visible
            deprecated: `${commonStyle} bg-red-500/50 dark:bg-red-200/70`, // Make red more visible
        };
        return styles[status] || "";
    };

    const getBadgeStyle = (status: string): string => {
        const styles: { [key: string]: string } = {
            proposal: "mr-12 text-gray-700", // Light gray
            beta: "mr-12 text-orange-800", // Light orange
            newly: "mr-12 text-blue-800", // Light blue
            stable: "mr-12 text-green-800", // Light green
            deprecated: "mr-12 text-red-800", // Light red
        };
        return styles[status] || "";
    };

    return (
        <>
            <Popover>
                <PopoverTrigger className={getCommandLineStyle(command.status)}>
                    <div
                        className={`flex items-center ${getCommandLineStyle(command.status)}`}
                    >
                        <div className="flex items-center ml-8">
                            {command.status === "proposal" && (
                                <HandHelping
                                    className={getBadgeStyle(command.status)}
                                />
                            )}
                            {command.status === "beta" && (
                                <CircleAlert
                                    className={getBadgeStyle(command.status)}
                                />
                            )}
                            {command.status === "newly" && (
                                <Check
                                    className={getBadgeStyle(command.status)}
                                />
                            )}
                            {command.status === "stable" && (
                                <CircleCheckBig
                                    className={getBadgeStyle(command.status)}
                                />
                            )}
                            {command.status === "deprecated" && (
                                <CircleX
                                    className={getBadgeStyle(command.status)}
                                />
                            )}
                            <span className="text-black">
                                {command.command}
                            </span>
                        </div>
                    </div>
                </PopoverTrigger>
                <PopoverContent className="text-black bg-white dark:text-white dark:bg-[var(--ifm-background-color)]">
                    <div className="flex flex-col gap-2 text-black dark:text-white">
                        <div className="flex items-center gap-2">
                            <span className="font-medium">
                                {command.status === "proposal" &&
                                    "提案中のコマンド"}
                                {command.status === "beta" &&
                                    "ベータ版のコマンド"}
                                {command.status === "newly" && "新しいコマンド"}
                                {command.status === "stable" &&
                                    "安定版のコマンド"}
                                {command.status === "deprecated" &&
                                    "非推奨のコマンド"}
                            </span>
                        </div>
                        <div className="h-px bg-gray-200 dark:bg-gray-700" />
                        <div className="flex flex-col gap-1">
                            <p className="text-sm">
                                エイリアス: {command.aliases.join(", ")}
                            </p>
                            <p className="text-sm mb-0">
                                パーミッション: {command.permission}
                            </p>
                        </div>
                    </div>
                </PopoverContent>
            </Popover>
            <p className="pt-2 pl-8 text-black dark:text-white">
                説明: {command.description}{" "}
                {command.tags.includes("player") ? "" : "(管理者向け)"}
            </p>
        </>
    );
};
