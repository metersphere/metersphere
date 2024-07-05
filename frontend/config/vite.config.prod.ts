import configCompressPlugin from './plugin/compress';
import configVisualizerPlugin from './plugin/visualizer';
import baseConfig from './vite.config.base';
import legacy from '@vitejs/plugin-legacy';
import { mergeConfig } from 'vite';

export default mergeConfig(
  {
    mode: 'production',
    plugins: [
      configCompressPlugin('gzip'),
      configVisualizerPlugin(),
      // 兼容性配置，配合package.json中的browserslist使用
      legacy({
        targets: ['defaults', 'not IE 11'],
      }),
    ],
    build: {
      rollupOptions: {
        output: {
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia', '@vueuse/core', 'vue-i18n'],
            arco: ['@arco-design/web-vue'],
            chart: ['echarts', 'vue-echarts'],
            codeEditor: ['monaco-editor'],
          },
        },
      },
      chunkSizeWarningLimit: 2000,
    },
  },
  baseConfig
);
