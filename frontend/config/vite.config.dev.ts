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
          target: 'https://qadevtest.fit2cloud.com/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/ws/, ''),
          ws: true,
        },
        '/front': {
          target: 'https://qadevtest.fit2cloud.com/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front/, ''),
        },
        '/file': {
          target: 'https://qadevtest.fit2cloud.com/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/file/, ''),
        },
        '/attachment': {
          target: 'https://qadevtest.fit2cloud.com/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/attachment/, ''),
        },
        '/bug/attachment': {
          target: 'https://qadevtest.fit2cloud.com/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/bug\/attachment/, ''),
        },
        '/plugin/image': {
          target: 'https://qadevtest.fit2cloud.com/',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/front\/plugin\/image/, ''),
        },
        '/base-display': {
          target: 'https://qadevtest.fit2cloud.com/',
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
