## 提交规范

### 1. 提交消息格式

```bin
<Type>[optional Scope]: <Description>
[optional body]

[optional footer]
```

#### Type 提交的类型

> 表示提交的目的或影响的范围

- feat: 新功能（A new feature）
- fix: 修复 bug（A bug fix）
- docs: 文档变更（Documentation changes）
- style: 代码样式调整（Code style changes）
- refactor: 代码重构（Code refactoring）
- test: 测试相关的变更（Test-related changes）
- chore: 构建过程或工具变动（Build process or tooling changes）
- perf: 性能优化（Performance optimization）
- ci: CI/CD 相关的变动（Changes to the CI/CD configuration or scripts）
- revert: 撤销之前的提交（Revert a previous commit）

#### Scope 作用域

提交的作用域，表示本次提交影响的部分代码或模块，可以根据项目的需要选择性地添加。

#### Description 描述

简明扼要地描述本次提交的内容

#### body 正文

可选的详细描述，可以包含更多的信息和上下文

#### footer 脚注

可选的脚注，通常用于引用相关的问题编号或关闭问题。

## 示例提交消息：

```
feat(user): add login functionality

- Add login form and authentication logic
- Implement user authentication API endpoints

Closes #123
```

在这个示例中，提交类型为 feat（新功能），作用域为 user，描述了添加登录功能的内容。正文部分提供了更详细的说明，并引用了问题编号。

## 架构总览

```
├── babel.config.js						// babel配置，支持JSX
├── commitlint.config.js			// commitlint配置，校验commit信息
├── components.d.ts						// 组件注册TS声明
├── config										// 项目构建配置
│   ├── plugin								// 构建插件
│   ├── utils									// 构建工具方法
│   ├── vite.config.base.ts		// vite基础配置
│   ├── vite.config.dev.ts		// vite开发环境配置
│   └── vite.config.prod.ts		// vite 生产配置
├── index.html								// 单页面html模板
├── src
│   ├── App.vue								// 应用入口vue
│   ├── api										// 项目请求api封装
│   │   ├── http							// axios封装
│   │   ├── modules						// 各业务模块的请求方法
│   │   ├── requrls						// 按业务模块划分的接口地址
│   ├── assets								// 全局静态资源
│   │   ├── font
│   │   ├── icon-font
│   │   ├── images
│   │   ├── style
│   │		├── svg
│   ├── components						// 组件
│   ├── config								// 全局配置，常量类、JSON
│   ├── directive							// 自定义指令集
│   ├── enums									// 全局枚举定义
│   ├── hooks									// 全局hooks集
│   ├── layout								// 应用布局组件
│   ├── locale								// 国际化配置
│   ├── main.ts								// 项目主入口
│   ├── models								// 全局数据模型定义
│   ├── router								// 路由
│   ├── store									// pinia状态库
│   ├── types									// 全局TS声明
│   ├── utils									// 公共工具方法
│   └── views
│       ├── modules						// 页面模块
│       └── base							// 公共页面，403、404等
│   ├── env.d.ts							// 环境信息TS类型声明
└── .env.development					// 开发环境变量声明
└── .env.production						// 生产环境变量声明
└── .eslintrc.js							// eslint配置
└── .prettierrc.js						// prettier配置
└── tsconfig.json							// 全局TS配置
```

<a name="dd14f832"></a>

## -状态管理模块设计

Vue3 状态管理方案采用的是`pinia`，API 风格与`Redux`类的状态管理库类似，也是通过模块化的方式注册`模块store`，每个`store`中包含数据仓库`state`、数据包装过滤`getter`、同步/异步操作`action`。与`Vuex`相比，`pinia`提供了`compasition-API`、完整支持 TS 以及可扩展的`Plugin`功能。<br />整体模块划分为业务模块`modules/*`、注册入口`index`以及插件`plugins/*`。<br />首先，在`store/index.ts`中声明注册`pinia`并引入自定义的插件：

```typescript
import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import { debouncePlugin } from './plugins';
import useXXStore from './modules/xx';

const pinia = createPinia();
// 插件会在实例创建后应用，因此插件不能在pinia实例创建前使用
pinia.use(debouncePlugin).use(piniaPluginPersistedstate);
export { useXXStore }; // 导出模块store
export default pinia;
```

然后，在项目入口`mian.ts`中使用：

```typescript
import { createApp } from 'vue';
import store from '@/store';
import app from '@/App.vue';

createApp(app).use(store).mount('#app');
```

<a name="piniaPlugins.ts"></a>

### plugins

在此文件内编写`pinia`插件，并在注册的时候引入使用即可：

```typescript
import { debounce, isObject } from 'lodash-es';
// 首先得声明插件使用到的额外属性，因为pinia的TS类型声明中，每个store只有三个原生属性state、getters、actions，若没有使用到额外属性则无需声明
declare module 'pinia' {
  export interface DefineStoreOptionsBase<S, Store> {
    cache?: Partial<CacheType<S, Store>>; // 缓存配置
    debounce?: Partial<Record<keyof StoreActions<Store>, number>>; // 节流配置
  }
}
// 基于lodash的防抖函数封装，读取store中cache配置的属性，针对已配置的属性更改操作进行防抖处理
export const debouncePlugin = ({ options, store }: PiniaPluginContext) => {
  if (options.debounce) {
    return Object.keys(options.debounce).reduce((debounceActions: debounceAction, action) => {
      debounceActions[action] = debounce(store[action], options.debounce![action]);
      return debounceActions;
    }, {});
  }
};
```

上述自定义插件，在 store 中如下配置：

```typescript
export const useStore = defineStore('demo', {
  state: () => ({
    testDebounce: '',
  }),
  getters: {},
  actions: {},
  debounce: {
    // 防抖设置
    testDebounce: 500, // 值为防抖缓冲时间
  },
});
```

<a name="520bb47a"></a>

## -网络模块设计

网络模块包含：请求 url 封装`api/requrls/*`、请求方法封装`api/modules/*`、请求工具封装`api/http/*`。
<a name="slaQE"></a>

### requrls

将项目接口地址收敛至此文件夹下管理，避免出现一个项目多个重复接口 url、方便接口地址复用且方便统一处理

```typescript
export const LoginUrl = '/api/user/login';
export const LogoutUrl = '/api/user/logout';
export const GetUserInfoUrl = '/api/user/info';
export const GetMenuListUrl = '/api/user/menu';
```

<a name="y561q"></a>

### modules

将项目实际请求方法按业务模块划分，统一管理实际业务请求，将请求方法与接口地址解耦

```typescript
import axios from 'axios';
import { LoginUrl, LogoutUrl, GetUserInfoUrl, GetMenuListUrl } from '@/api/requrls/user';
import type { RouteRecordNormalized } from 'vue-router';
import type { UserState } from '@/store/modules/user/types';
import type { LoginData, LoginRes } from '@/models/user';

export function login(data: LoginData) {
  return axios.post<LoginRes>(LoginUrl, data);
}

export function logout() {
  return axios.post<LoginRes>(LogoutUrl);
}

export function getUserInfo() {
  return axios.post<UserState>(GetUserInfoUrl);
}

export function getMenuList() {
  return axios.post<RouteRecordNormalized[]>(GetMenuListUrl);
}
```

最终通过`index.ts`将请求方法暴露出去

```typescript
export * from './modules/user';
export * from './modules/dashboard';
export * from './modules/message';
```

<a name="0f0a9627"></a>

#### 请求工具封装

基于`axios`封装请求方法，提供`form-data/json/urlencoded`格式的数据处理、自定义头部处理、响应拦截错误处理、分级提示（modal、message、none）、get 请求防缓存。

```typescript
// 分级错误信息提示，none为静默模式即不提示、modal为对话框提示、message为tips消息提示
// ErrorMessageMode默认为message模式，通过传入请求方法的options.errorMessageMode入参判断
export type ErrorMessageMode = 'none' | 'modal' | 'message' | undefined;

// 三种入参数据格式
export enum ContentTypeEnum {
  // json
  JSON = 'application/json;charset=UTF-8',
  // form-data qs序列化处理
  FORM_URLENCODED = 'application/x-www-form-urlencoded;charset=UTF-8',
  // form-data  upload文件流处理
  FORM_DATA = 'multipart/form-data;charset=UTF-8',
}

// 对上传文件请求入参格式额外定义
export interface UploadFileParams {
  // 除文件流外的参数
  data?: Recordable;
  // 文件流字段名
  name?: string;
  // 文件内容
  file: File | Blob;
  // 文件名
  filename?: string;
  // 其他parmas
  [key: string]: any;
}

// 基准返回数据格式
export interface Result<T = any> {
  code: number;
  type: 'success' | 'error' | 'warning';
  message: string;
  result: T;
}
```

<a name="c53518d5"></a>

## -directive 指令集

指令入口`index.ts`导入并注册定义的全部指令

```typescript
import { App } from 'vue';
import permission from './permission';

export default {
  install(Vue: App) {
    Vue.directive('permission', permission);
  },
};
```

权限指令（或其他自定义指令）在同级目录下创建，以指令名称命名文件夹，下面是示例：

```typescript
import { DirectiveBinding } from 'vue';
import { useUserStore } from '@/store';

function checkPermission(el: HTMLElement, binding: DirectiveBinding) {
  const { value } = binding;
  const userStore = useUserStore();
  const { role } = userStore;

  if (Array.isArray(value)) {
    if (value.length > 0) {
      const permissionValues = value;
      const hasPermission = permissionValues.includes(role);
      if (!hasPermission && el.parentNode) {
        el.parentNode.removeChild(el);
      }
    }
  } else {
    throw new Error(`need roles! Like v-permission="['admin','user']"`);
  }
}

export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding);
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding);
  },
};
```

<a name="Kw0kR"></a>

## -hooks

全局抽象钩子集（与 Vue2 的 mixins 类似），这里只写业务逻辑的钩子！！！通过`@vueuse/core`我们已经可以得到非常多实用的钩子函数，没必要重复造轮子，在编写钩子功能前先去[Function List](https://vueuse.org/functions.html)查看是否已经有相同功能的钩子了，没有再自己写。导出钩子`export default function useXxx`，以权限钩子示例：

```typescript
import { RouteLocationNormalized, RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/store';

export default function usePermission() {
  const userStore = useUserStore();
  return {
    accessRouter(route: RouteLocationNormalized | RouteRecordRaw) {
      // do something
    },
    findFirstPermissionRoute(_routers: any, role = 'admin') {
      // do something
    },
    // You can add any rules you want
  };
}
```

<a name="In07K"></a>

## -layout

项目布局设计模块，将最上层布局组件放置此文件夹内，统一管理项目各类上层布局（若存在，例如左侧菜单-右侧内容布局、顶层菜单-中间内容-底部页脚布局等等），示例：

```vue
<template>
  <!-- router-view内为实际路由切换变化的内容，视为路由页面容器 -->
  <router-view v-slot="{ Component, route }">
    <!-- transition为页面录音切换时提供进出动画，平滑过渡页面渲染 -->
    <transition name="fade" mode="out-in" appear>
      <component :is="Component" v-if="!route.meta.isCache" :key="route.fullPath" />
      <!-- keep-alive提供组件状态缓存，以便快速渲染组件内容 -->
      <keep-alive v-else>
        <component :is="Component" :key="route.fullPath" />
      </keep-alive>
    </transition>
  </router-view>
</template>
```

<a name="KZkaZ"></a>

## -models

全局数据模型，将涉及请求、组件 props、状态库的公共属性抽象为数据模型，在此文件夹内声明，保证全局数据的类型统一、方便维护。下面是请求模型示例：

```typescript
export interface HttpResponse<T = unknown> {
  status: number;
  msg: string;
  code: number;
  data: T;
}
```

也可以是业务相关模型：

```typescript
export interface LoginData {
  username: string;
  password: string;
}

export interface LoginRes {
  token: string;
}
```

<a name="d3Gdu"></a>

## -router

项目路由管理模块，入口文件`index.ts`注册并暴露全部路由，以模块命名文件夹划分模块路由放置在`routes/*`下；`guard/*`下放置路由导航控制，包含权限、登录重定向等；`app-menus/index.ts`为菜单相关的路由信息；`constants.ts`定义路由常量，包括路由白名单、重定向路由名、默认主页路由信息等
<a name="YZWcg"></a>

## -enums

全局的枚举类型声明模块，将可枚举的类型统一管理，以`类型+Enum`命名，方便维护。示例如下：

```typescript
/**
 * @description: Request result set
 */
export enum ResultEnum {
  SUCCESS = 0,
  ERROR = 1,
  TIMEOUT = 401,
  TYPE = 'success',
}

/**
 * @description: request method
 */
export enum RequestEnum {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
}

/**
 * @description:  contentTyp
 */
export enum ContentTypeEnum {
  // json
  JSON = 'application/json;charset=UTF-8',
  // form-data qs
  FORM_URLENCODED = 'application/x-www-form-urlencoded;charset=UTF-8',
  // form-data  upload
  FORM_DATA = 'multipart/form-data;charset=UTF-8',
}
```

<a name="I0KEf"></a>

## -locale

国际化模块，存放项目声明的国际化配置，按语种划分模块。模块入口文件为`index.ts`，负责定义菜单、导航栏等公共的国际化配置，其他的按系统功能声明并导入`index.ts`，还有项目内页面组件的国际化配置也需要导入至`index.ts`，页面组件的国际化配置在页面的目录下声明，如：`views/dashboard/workbench/locale`。

```typescript
import { createI18n } from 'vue-i18n';
import en from './en-US';
import cn from './zh-CN';

export const LOCALE_OPTIONS = [
  { label: '中文', value: 'zh-CN' },
  { label: 'English', value: 'en-US' },
];
const defaultLocale = localStorage.getItem('MS-locale') || 'zh-CN';
const i18n = createI18n({
  locale: defaultLocale,
  fallbackLocale: 'en-US',
  legacy: false,
  allowComposition: true,
  messages: {
    'en-US': en,
    'zh-CN': cn,
  },
});

export default i18n;
```

<a name="fZfbw"></a>

## -types

项目级别的类型声明，与业务无关的类型声明，与`models`、`enums`不同的是，这里声明的是项目模块级别的类型，或者工具模块的类型声明，例如：`axios`的配置声明、第三方插件不提供 TS 支持但需要我们自定义的声明等
<a name="hvu5O"></a>

## -utils

公共方法、工具，按功能类型命名文件，单个文件内工具方法的功能要对应命名
<a name="FFTqz"></a>

## -views

页面模块，按功能模块划分，公共模块有`login`、`base`，其中`base`模块内包含 404、403 页面等
<a name="RwVcu"></a>

## -theme 主题配置

1. 去 Design Lab 创建主题 https://arco.design/themes/home
2. 以`ms-theme-` 命名为开头
3. 点击页面的配置主题
   **“CSS 变量” + “Tailwind 配置变量” + “基于 css 变量自行计算混合色覆盖 arco-theme 变量”**

## -.env.\*环境变量配置

在`vite`中内置了环境变量配置功能，只需要在项目根目录下创建以`.env.*`开头的文件，在内部写入配置的变量即可，默认`.env`为生产环境、`.env.development`为开发环境、`.env.XXX`为自定义`XXX`环境（注意：自定义环境需要在`package.json`的项目运行命令后加入`--mode XXX`），各类环境变量配置示例如下（ ⚠️ 注意！！环境变量文件内使用注释要用`#`，不能使用`//`）:

```bash
# .env.production 为生产环境配置
NODE_ENV=production # 代码中通过import.meta.env.NODE_ENV访问
VITE_STG=1 # 自定义变量必须用VITE_开头，代码中通过import.meta.env.VITE_XXX访问

# .env.development 为开发环境配置
NODE_ENV=development
VITE_APP_ENV = dev
VITE_APP_TITLE = 我是标题

# .env.XXX 为自定义环境配置
VITE_MYENV = 1 # 代码中通过import.meta.env.VITE_MYENV访问
```

⚠️ 上述环境变量在代码中正常都可以用`import.meta.env.VITE_XXX`访问，但是在`vite.config.ts`配置文件中无法使用此方法访问，原因是此访问链是 vite 在初始化后通过读取本地`.env.XXX`文件并注入到`import.meta`中的，但是在`vite.config.ts`中 vite 此时还未初始化，所以无法通过上述方法访问，应通过下面方法访问：

```typescript
import { defineConfig, loadEnv } from 'vite'; // 导入loadEnv方法
loadEnv(mode, process.cwd()).VITE_XXX; // 在需要访问env里变量的地方使用此方法访问即可
```

<a name="c61428f2"></a>

## -TailwindCSS 配置

```javascript
module.exports = {
  content: ['./index.html', './src/**/*.{html,js,vue}', './src/*.{html,js,vue}'], // 需要解析的文件路径
  theme: {
    // 自定义主题配置
    backgroundColor: {
      // 自定义背景色
      menuHover: '#272D39',
      headerBg: '#191E29',
    },
    textColor: (theme) => ({
      // 自定义字体颜色
      ...theme('colors'), // 这里必须解构原本有的颜色，不然会导致在页面style中使用@apply应用内部字体颜色类的时候报错找不到内部字体颜色类
      '40Gray': 'rgba(255,255,255,0.40)',
      '65Gray': 'rgba(255,255,255,0.65)',
    }),
    extend: {},
  },
  plugins: [],
};
```

<a name="c7baab5c"></a>

## -TS 配置-tsconfig.json

```json
{
  "include": [
    "src/**/*.ts",
    "src/**/*.d.ts",
    "src/**/*.tsx",
    "src/**/*.vue",
    "src/components.d.ts",
    "auto-imports.d.ts"
  ], // TS解析路径配置
  "exclude": ["node_modules"],
  "compilerOptions": {
    "allowJs": true, // 允许编译器编译JS，JSX文件
    "noEmit": true,
    "target": "esnext", // 使用es最新语法
    "useDefineForClassFields": true,
    "allowSyntheticDefaultImports": true, // 允许异步导入模块，配合自动导入插件使用
    "module": "esnext", // 使用ES模块语法
    "moduleResolution": "node",
    "strict": true, // 严格模式
    "jsx": "preserve",
    "sourceMap": true, // 代码映射
    "resolveJsonModule": true,
    "isolatedModules": true,
    "esModuleInterop": true,
    "lib": ["esnext", "dom"],
    "skipLibCheck": true, // 跳过node依赖包语法检查
    "types": [
      // "vite-plugin-svg-icons/client"
    ], // 手动导入TS类型声明文件
    "baseUrl": ".",
    "paths": {
      // 路径映射
      "@/*": ["./src/*"],
      "#/*": ["types/*"]
    }
  }
}
```

<a name="50a6c7f6"></a>

## -vite 配置-插件详解

`vite`构建生产资源使用`rollup`打包，所以可以在`vite`中使用`rollup`支持的所有插件。

- 组件自动导入，使用`unplugin-auto-import/vite`插件实现自动导入组件、`unplugin-vue-components/vite`插件按需导入自定义组件，具体用法如下：

```typescript
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ArcoResolver } from 'unplugin-vue-components/resolvers'
import { vitePluginForArco } from '@arco-plugins/vite-vue'

export default () => defineConfig({
  plugins: [
    // 自定义组件自动引入
    AutoImport({å
      dts: 'src/auto-import.d.ts', // 输出声明文件地址(在使用typescript时必须配置，不然会导致页面使用未导入的组件时报错)
      resolvers: [ElementPlusResolver()], // ElementPlus自动导入
    }),
    // 自定义组件按需引入
    Components({
      dts: 'src/components.d.ts', // 输出声明文件地址(在使用typescript时必须配置，不然会导致页面使用未导入的组件时报错)
      dirs: ['src/components'], // 按需加载的文件夹
      resolvers: [
        ArcoResolver(), // ArcoDesignVue按需加载
      ],
    }),
    // 样式自动导入
    vitePluginForArco({})
  ]
})
```

- 使用`vite-plugin-svg-icons`插件实现自动加载 svg 图片，并通过封装 svg 组件的方式，一行代码使用 svg，配置如下：

```typescript
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons';

export default () =>
  defineConfig({
    plugins: [
      createSvgIconsPlugin({
        // 指定svg读取的文件夹
        iconDirs: [resolve(process.cwd(), 'src/assets/icons/svg')],
        // 指定icon的读取名字，使用svg文件名为icon名字
        symbolId: 'icon-[dir]-[name]',
      }),
    ],
  });
```

- 使用`vite`自带插件`loadEnv`读取环境信息，可作环境判断，使用`rollup-plugin-visualizer`插件可分析打包后的文件体积，配置如下：

```typescript
import { defineConfig, loadEnv } from 'vite';
import { visualizer } from 'rollup-plugin-visualizer';

// 这里的mode参数为package.json文件配置的环境参数，使用`--mode XXX`，如"report: rimraf dist && vite build --mode analyze"
export default ({ mode }) =>
  defineConfig({
    plugins: [
      // 这里通过--mode analyze配置为分析模式，使用visualizer插件分析方法，输出report.html分析报告
      loadEnv(mode, process.cwd()).VITE_ANALYZE === 'Y'
        ? visualizer({ open: true, brotliSize: true, filename: 'report.html' })
        : null,
    ],
  });
```

<a name="0c0978d0"></a>

## -vite 配置-build 详解

上面讲述了插件应用，下面讲解除了插件外，在`vite`中的 build 构建生产资源时，`rollupOptions`的配置：

- `output`为输出产物配置，我们可以通过`manualChunks`去配置分包策略（与`webpack`分包机制类似），配置如下：

```typescript
import { mergeConfig } from 'vite';
import baseConfig from './vite.config.base';
import configCompressPlugin from './plugin/compress';
import configVisualizerPlugin from './plugin/visualizer';
import configArcoResolverPlugin from './plugin/arcoResolver';
import configImageminPlugin from './plugin/imagemin';

export default mergeConfig(
  {
    mode: 'production',
    plugins: [
      configCompressPlugin('gzip'),
      configVisualizerPlugin(),
      configArcoResolverPlugin(),
      configImageminPlugin(),
    ],
    build: {
      rollupOptions: {
        output: {
          manualChunks: {
            arco: ['@arco-design/web-vue'],
            chart: ['echarts', 'vue-echarts'],
            vue: ['vue', 'vue-router', 'pinia', '@vueuse/core', 'vue-i18n'],
          },
        },
      },
      chunkSizeWarningLimit: 2000,
    },
  },
  baseConfig
);
```

## 本地生产环境调试

需安装 docker： https://www.docker.com/, 选择对应系统版本安装。

```bash
cd frontend/
pnpm run build:local
docker build -t metersphere/ms-v3 .
docker run -d -p 5100:5100 --name ms-v3 metersphere/ms-v3
```
