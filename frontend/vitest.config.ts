import { mergeConfig } from 'vite';
import { defineConfig } from 'vitest/config';
import viteConfig from './config/vite.config.dev';

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
    },
  })
);
