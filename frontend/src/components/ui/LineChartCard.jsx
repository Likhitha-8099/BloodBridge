import React from 'react';
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts';
import ChartContainer from './ChartContainer';

/**
 * Reusable line chart component for displaying numerical trends over time.
 */
export default function LineChartCard({ 
  title, 
  data = [], 
  xKey = 'name', 
  yKey = 'value', 
  name = 'Value', 
  color = '#E11D48' 
}) {
  return (
    <ChartContainer title={title}>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 10, right: 20, left: -20, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f8fafc" />
          <XAxis 
            dataKey={xKey} 
            stroke="#94a3b8" 
            fontSize={10} 
            tickLine={false} 
            axisLine={false}
          />
          <YAxis 
            stroke="#94a3b8" 
            fontSize={10} 
            tickLine={false} 
            axisLine={false}
          />
          <Tooltip
            contentStyle={{ 
              backgroundColor: '#ffffff', 
              borderRadius: '16px', 
              border: '1px solid #f1f5f9', 
              fontSize: '11px',
              boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.05)'
            }}
          />
          <Legend wrapperStyle={{ fontSize: '10px', paddingTop: '8px' }} />
          <Line 
            type="monotone" 
            dataKey={yKey} 
            name={name} 
            stroke={color} 
            strokeWidth={2.5} 
            dot={{ r: 4, strokeWidth: 1 }}
            activeDot={{ r: 6 }} 
          />
        </LineChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
}
