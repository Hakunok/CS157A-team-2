import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Any request starting with /api will be forwarded
      '/api': {
        // This MUST match your Tomcat URL and context path
        target: 'http://localhost:8080/airchive_war_exploded',
        changeOrigin: true,
      }
    }
  }
})
