import React from 'react';

interface TableDefinitionProps {
  title: string;
  columns: {
    name: string;
    type: string;
    example: React.ReactNode;
    description: string;
  }[];
}

export const TableDefinition: React.FC<TableDefinitionProps> = ({ title, columns }) => {
  return (
    <div>
      <h3>{title}</h3>
      <table>
        <thead>
          <tr>
            <th className='w-1/4'>column name</th>
            <th className='w-1/4'>type</th>
            <th className='w-1/4'>description</th>
            <th className='w-1/4'>example</th>
          </tr>
        </thead>
        <tbody>
          {columns.map((column, index) => {
            const style = "overflow-hidden text-overflow-ellipsis w-1/4"
            return (
              <tr key={index}>
                <td className={style}>{column.name}</td>
                <td className={style}>{column.type}</td>
                <td className={style}>{column.description}</td>
                <td className={style}>{column.example}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  );
}; 