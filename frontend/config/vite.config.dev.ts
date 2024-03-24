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
        '/ws': {
          target: 'http://192.168.8.200:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/ws/, ''),
          ws: true,
        },
        '/front': {
          target: 'http://192.168.8.200:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front/, ''),
        },
        '/file': {
          target: 'http://192.168.8.200:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/file/, ''),
        },
        '/attachment': {
          target: 'http://192.168.8.200:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/attachment/, ''),
        },
        '/bug/attachment': {
          target: 'http://192.168.8.200:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/bug\/attachment/, ''),
        },
        '/plugin/image': {
          target: 'http://192.168.8.200:8081/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/plugin\/image/, ''),
        },
        '/base-display': {
          target: 'http://192.168.8.200:8081/',
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
