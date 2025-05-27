// eslint-disable-next-line @typescript-eslint/no-var-requires
const path = require('path');

module.exports = {
  root: true,
  parser: 'vue-eslint-parser',
  parserOptions: {
    // Parser that checks the content of the <script> tag
    parser: '@typescript-eslint/parser',
    sourceType: 'module',
    ecmaVersion: 2020,
    ecmaFeatures: {
      jsx: true,
    },
  },
  env: {
    'browser': true,
    'node': true,
    'vue/setup-compiler-macros': true,
  },
  plugins: ['@typescript-eslint', 'simple-import-sort'],
  extends: [
    // Airbnb JavaScript Style Guide https://github.com/airbnb/javascript
    'airbnb-base',
    'plugin:@typescript-eslint/recommended',
    'plugin:import/recommended',
    'plugin:import/typescript',
    'plugin:vue/vue3-recommended',
    'plugin:prettier/recommended',
    './.eslintrc-auto-import.json',
  ],
  settings: {
    'import/resolver': {
      typescript: {
        project: path.resolve(__dirname, './tsconfig.json'),
      },
    },
  },
  rules: {
    'prettier/prettier': 1,
    // Vue: Recommended rules to be closed or modify
    'vue/require-default-prop': 0,
    'vue/singleline-html-element-content-newline': 0,
    'vue/max-attributes-per-line': 0,
    // Vue: Add extra rules
    'vue/custom-event-name-casing': [2, 'camelCase'],
    'vue/no-v-text': 1,
    'vue/padding-line-between-blocks': 1,
    'vue/require-direct-export': 1,
    'vue/multi-word-component-names': 0,
    // Allow @ts-ignore comment
    '@typescript-eslint/ban-ts-comment': 0,
    '@typescript-eslint/no-unused-vars': 1,
    '@typescript-eslint/no-empty-function': 1,
    '@typescript-eslint/no-explicit-any': 0,
    '@typescript-eslint/no-duplicate-enum-values': 0,
    'consistent-return': 'off',
    'vue/return-in-computed-property': ['off'],
    'vue/no-side-effects-in-computed-properties': 'off',
    'import/extensions': [
      2,
      'ignorePackages',
      {
        js: 'never',
        jsx: 'never',
        ts: 'never',
        tsx: 'never',
      },
    ],
    'no-debugger': 2,
    'no-param-reassign': 0,
    'prefer-regex-literals': 0,
    'import/no-extraneous-dependencies': 0,
    'import/no-cycle': 'off',
    'import/order': 'off',
    'class-methods-use-this': 'off',
    'global-require': 0,
    'no-plusplus': 'off',
    'no-underscore-dangle': 'off',
    'vue/attributes-order': 1,
    'simple-import-sort/exports': 'error',
    'no-case-declarations': 'off',
    // 调整导入语句的顺序
    'simple-import-sort/imports': [
      'error',
      {
        groups: [
          [
            '^vue$',
            '^vue-router$',
            '^vue-i18n$',
            '^pinia$',
            '^@vueuse/core$',
            '^@arco-design/web-vue$',
            '^monaco-editor$',
            '^lodash-es$',
            '^axios$',
            '^dayjs$',
            '^jsencrypt$',
            '^echarts$',
            '^color$',
            '^localforage$',
            'vue-draggable-plus',
            'jsonpath-plus',
            'lossless-json',
            'vue-element-plus-x',
          ], // node依赖
          ['.*/assets/.*', '^@/assets$'], // 项目静态资源
          ['^@/components/pure/.*', '^@/components/business/.*', '.*\\.vue$'], // 组件
          [
            '^@/api($|/.*)',
            '^@/config($|/.*)',
            '^@/directive($|/.*)',
            '^@/hooks($|/.*)',
            '^@/locale($|/.*)',
            '^@/router($|/.*)',
            '^@/store($|/.*)',
            '^@/utils($|/.*)',
          ], // 项目公共模块
          ['^@/models($|/.*)', '^@/enums($|/.*)'], // model、enum
          ['^type'], // 第三方类型声明 or 全局类型声明
        ],
      },
    ],
  },
  // 对特定文件进行配置
  overrides: [
    {
      files: ['src/enums/**/*.ts'],
      rules: {
        'no-shadow': 'off', // eslint会报错提示重复声明，暂未找到问题原因，先关闭
        // 可以在这里添加更多的规则禁用
      },
    },
  ],
  globals: {
    // 在这里添加全局变量
    NodeJS: 'readonly',
  },
};
