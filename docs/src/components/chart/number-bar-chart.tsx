import ReactECharts from 'echarts-for-react';
import React from 'react';
import * as echarts from 'echarts/core';
import {
    LineChart
} from 'echarts/charts';
import { WeatherType } from '@site/src/lib/weather-random';

type NumberBarChartProps = {
    data: { x: number, y: string }[]
    size: number
}

export const NumberBarChart = ({ data, size }: NumberBarChartProps) => {

  const xLabels = [...new Set(data.map((d) => d.y.toString()))].sort()
  const yData = xLabels.map((y) => data.filter((d) => d.y.toString() === y).length)
  const option = {
      xAxis: {
        type: 'category',
        data: xLabels
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: yData,
          type: 'bar'
        }
      ]
    };
      
      echarts.use([LineChart])
    return (
        <ReactECharts echarts={echarts} option={option} />
    )
}