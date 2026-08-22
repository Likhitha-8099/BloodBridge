import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  build: {
    chunkSizeWarningLimit: 600,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('recharts') || id.includes('d3-')) {
              return 'charts';
            }
            if (id.includes('framer-motion')) {
              return 'framer-motion';
            }
            if (id.includes('firebase')) {
              return 'firebase';
            }
            if (id.includes('@stomp') || id.includes('sockjs-client')) {
              return 'websocket';
            }
            if (id.includes('@tanstack/react-query')) {
              return 'react-query';
            }
            if (id.includes('lucide-react')) {
              return 'lucide-icons';
            }
            if (id.includes('react-router-dom') || id.includes('@remix-run')) {
              return 'router';
            }
            if (id.includes('react') || id.includes('react-dom') || id.includes('zustand') || id.includes('axios')) {
              return 'vendor';
            }
          }
        },
      },
    },
  },
})
