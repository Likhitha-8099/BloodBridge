import React from 'react';
import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip, Legend } from 'recharts';
import ChartContainer from './ChartContainer';

// Color Palette for Pie Slices
const COLORS = [
  '#E11D48', // Red
  '#2563EB', // Blue
  '#059669', // Green
  '#D97706', // Yellow/Amber
  '#7C3AED', // Purple
  '#DB2777', // Pink
  '#0D9488', // Teal
  '#475569', // Slate
];

/**
 * Reusable donut pie chart for showing distributions (e.g. blood groups, user roles).
 */
export default function PieChartCard({ title, data = [] }) {
  return (
    <ChartContainer title={title}>
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="45%"
            innerRadius={55}
            outerRadius={75}
            paddingAngle={4}
            dataKey="value"
          >
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip
            contentStyle={{ 
              backgroundColor: '#ffffff', 
              borderRadius: '16px', 
              border: '1px solid #f1f5f9', 
              fontSize: '11px',
              boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.05)'
            }}
          />
          <Legend 
            wrapperStyle={{ fontSize: '10px', paddingTop: '10px' }}
            layout="horizontal"
            align="center"
            verticalAlign="bottom"
          />
        </PieChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
}
