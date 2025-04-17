import React from 'react';
import { UUIDV7 } from '../faker/uuid';
import { UUIDV4 } from '../faker/uuid';
import { TableDefinition } from './table-difinition';

export const AccountsTable: React.FC = () => {
  const columns = [
    {
      name: 'id',
      type: 'uuid v7',
      example: <UUIDV7 />,
      description: '主キー MoripaFishing用のアカウントID',
    },
    {
      name: 'minecraft_unique_id',
      type: 'uuid v4 (varchar(36))',
      example: <UUIDV4 />,
      description: 'Minecraft用のプレイヤーID',
    },
    {
      name: 'player_name',
      type: 'varchar(16)',
      example: 'Notch',
      description: 'プレイヤーの名前',
    },
  ];

  return <TableDefinition title="Accounts" columns={columns} />;
}; 