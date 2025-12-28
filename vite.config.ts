import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'

// README 里的示例端口多为 8080。
// 你可以通过环境变量修改：VITE_API_BASE_URL，例如 http://localhost:8081

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiBase = env.VITE_API_BASE_URL || 'http://localhost:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: apiBase,
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})

