import configCompressPlugin from './plugin/compress';
import configImageminPlugin from './plugin/imagemin';
import configVisualizerPlugin from './plugin/visualizer';
import baseConfig from './vite.config.base';
import { mergeConfig } from 'vite';

export default mergeConfig(
  {
    mode: 'production',
    plugins: [configCompressPlugin('gzip'), configVisualizerPlugin(), configImageminPlugin()],
    build: {
      rollupOptions: {
        output: {
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia', '@vueuse/core', 'vue-i18n'],
            arco: ['@arco-design/web-vue'],
            chart: ['echarts', 'vue-echarts'],
          },
        },
      },
      chunkSizeWarningLimit: 2000,
    },
  },
  baseConfig
);
