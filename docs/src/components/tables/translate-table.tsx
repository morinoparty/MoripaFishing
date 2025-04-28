import type React from "react";

type TranslateTableProps = {
    columns: {
        description: string;
        name: string;
        example: React.ReactNode;
    }[];
};

export const TranslateTable: React.FC<TranslateTableProps> = ({ columns } ) => {
    return (
        <table>
            <thead>
                <tr>
                    <th>概要</th>
                    <th>タグ</th>
                    <th>例</th>
                </tr>
            </thead>
            <tbody>
                {columns.map((column, index) => {
                    const args = `<${column.name}>, <arg:${index}>`;
                    return (
                        <tr key={index}>
                            <td>{column.description}</td>
                            <td>{args}</td>
                            <td>{column.example}</td>
                        </tr>
                    );
                })}
            </tbody>
        </table>
    );
};
