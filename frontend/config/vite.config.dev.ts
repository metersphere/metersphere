/// <reference types="vitest" />
import baseConfig from './vite.config.base';
import { mergeConfig } from 'vite';
import eslint from 'vite-plugin-eslint';

export default mergeConfig(
  {
    mode: 'development',
    server: {
      open: true,
      fs: {
        strict: true,
      },
      proxy: {
        '/front': {
          target: 'http://172.16.200.18:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front/, ''),
        },
        '/file': {
          target: 'http://172.16.200.18:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/file/, ''),
        },
        '/attachment': {
          target: 'http://172.16.200.18:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/attachment/, ''),
        },
        '/plugin/image': {
          target: 'http://172.16.200.18:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/plugin\/image/, ''),
        },
        '/base-display': {
          target: 'http://172.16.200.18:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/base-display/, ''),
        },
      },
    },
    plugins: [
      eslint({
        cache: false,
        include: ['src/**/*.ts', 'src/**/*.tsx', 'src/**/*.vue'],
        exclude: ['node_modules'],
      }),
    ],
  },
  baseConfig
);
