import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backendTarget = env.VITE_BACKEND_TARGET || 'http://localhost:8080'

  return {
    plugins: [
      vue(),
      Components({
        dts: 'src/components.d.ts',
        resolvers: [ElementPlusResolver({ importStyle: 'css' })],
      }),
    ],
    server: {
      proxy: {
        '/api': { target: backendTarget, changeOrigin: true },
        '/actuator': { target: backendTarget, changeOrigin: true },
      },
    },
    optimizeDeps: {
      include: ['element-plus', '@element-plus/icons-vue'],
    },
    test: {
      environment: 'jsdom',
      include: ['src/**/*.test.ts'],
      setupFiles: ['./src/test/setup.ts'],
      css: true,
      server: {
        deps: { inline: [/element-plus/] },
      },
      coverage: {
        provider: 'v8',
        reporter: ['text', 'html'],
        exclude: ['src/shared/api/schema.d.ts', 'src/main.ts'],
      },
    },
  }
})
