import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://localhost:8080'

  return {
    plugins: [
      vue(),
      AutoImport({ resolvers: [ElementPlusResolver()] }),
      Components({ resolvers: [ElementPlusResolver()] })
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      host: '0.0.0.0',
      port: 5173,
      open: true,
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true
        },
        // 医生头像由后端静态资源 /avatars/doctor-N.png 提供（不带 /api 前缀），需单独代理
        '/avatars': {
          target: proxyTarget,
          changeOrigin: true
        }
      }
    },
    build: {
      sourcemap: false,
      chunkSizeWarningLimit: 1500
    }
  }
})
