import React from 'react';
import { UUIDV7 } from '../faker/uuid';
import { TableDefinition } from './table-difinition';
import { Timestamp } from '../faker/timestamp';

export const FishingLogsTable: React.FC = () => {
  const columns = [
    {
      name: 'id',
      type: 'uuid v7',
      example: <UUIDV7 />,
      description: '主キー フィッシングログID',
    },
    {
      name: 'angler_id',
      type: 'uuid v7',
      example: <UUIDV7 />,
      description: '釣り人ID',
    },
    {
      name: 'fish_id',
      type: 'string',
      example: 'cod',
      description: '魚ID',
    },
    {
      name: "world_id",
      type: "string",
      example: "default",
      description: "ワールドID",
    },
    {
      name : "size",
      type : "float",
      example : "100.0",
      description : "魚の大きさ"
    },
    {
      name : "worth",
      type : "float",
      example : "100.0",
      description : "魚の価値"
    },
    {
      name : "timestamp",
      type : "timestamp",
      example : <Timestamp />,
      description : "タイムスタンプ"
    }
  ];

  return <TableDefinition title="FishingLogs" columns={columns} />;
}; 