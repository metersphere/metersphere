import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import { resolve } from 'path';
import AutoImport from 'unplugin-auto-import/vite';
import { defineConfig } from 'vite';
// import configArcoStyleImportPlugin from './plugin/arcoStyleImport';
// import configArcoResolverPlugin from './plugin/arcoResolver';
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons';
import vueSetupExtend from 'vite-plugin-vue-setup-extend';
import svgLoader from 'vite-svg-loader';

export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueSetupExtend(),
    svgLoader({ svgoConfig: {} }),
    // configArcoResolverPlugin(),
    // configArcoStyleImportPlugin(),
    createSvgIconsPlugin({
      // 指定需要缓存的图标文件夹
      iconDirs: [resolve(process.cwd(), 'src/assets/svg'), resolve(process.cwd(), 'public/images')], // 与本地储存地址一致
      // 指定symbolId格式
      symbolId: 'icon-[name]',
    }),
    AutoImport({
      include: [
        /\.[tj]sx?$/, // .ts, .tsx, .js, .jsx
        /\.vue$/,
        /\.vue\?vue/, // .vue
        /\.md$/, // .md
      ],
      imports: ['vue'],
      dts: 'src/auto-import.d.ts',
      eslintrc: {
        enabled: true, // <-- this
      },
    }),
  ],
  resolve: {
    alias: [
      {
        find: '@',
        replacement: resolve(__dirname, '../src'),
      },
      {
        find: 'assets',
        replacement: resolve(__dirname, '../src/assets'),
      },
      {
        find: 'vue-i18n',
        replacement: 'vue-i18n/dist/vue-i18n.esm-bundler.js', // 解决 vue-i18n 依赖包报错
      },
      {
        find: 'vue',
        replacement: 'vue/dist/vue.esm-bundler.js', // compile template
      },
      {
        find: '@tiptap/pm/state',
        replacement: '@halo-dev/richtext-editor',
      },
      {
        find: '@tiptap/pm/model',
        replacement: '@halo-dev/richtext-editor',
      },
      {
        find: '@tiptap/core',
        replacement: '@halo-dev/richtext-editor',
      },
      {
        find: '@tiptap/pm/view',
        replacement: '@halo-dev/richtext-editor',
      },
    ],
    extensions: ['.ts', '.js', '.jsx', '.tsx', '.json', '.vue'],
  },
  define: {
    'process.env': {},
    // 定义特性标志
    '__VUE_OPTIONS_API__': true,
    '__VUE_PROD_DEVTOOLS__': false,
    // 设置hydration不匹配详细信息的标志
    '__VUE_PROD_HYDRATION_MISMATCH_DETAILS__': true,
  },
  css: {
    preprocessorOptions: {
      less: {
        modifyVars: {
          hack: `true; @import (reference) "${resolve('src/assets/style/var.less')}";`,
        },
        javascriptEnabled: true,
      },
    },
  },
});
