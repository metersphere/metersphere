/* eslint-disable simple-import-sort/imports */
import { createApp } from 'vue';
import ArcoVue from '@arco-design/web-vue';
import '@arco-themes/vue-metersphere-v3/index.less';

import MsIcon from '@/components/pure/ms-icon-font/index.vue';
import SvgIcon from '@/components/pure/svg-icon/index.vue';
import App from './App.vue';
import 'github-markdown-css/github-markdown-light.css';
// eslint-disable-next-line import/no-unresolved
import 'virtual:svg-icons-register';
import directive from './directive';
import { setupI18n } from './locale';
import router from './router';
import store from './store';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';
import '@/assets/style/global.less';
import localforage from 'localforage';
import VueDOMPurifyHTML from 'vue-dompurify-html';
import { getDefaultLocale } from './api/modules/user';
import useLocale from './locale/useLocale';
import applyPolyfills from '@/utils/polyfill';

// 局部兼容未生效，解决全局兼容富文本js源码报错导致，safari浏览器对富文本0bject.has0wn兼容引发空白问题
applyPolyfills();

async function bootstrap() {
  const app = createApp(App);

  app.use(store);
  // 注册国际化，需要异步阻塞，确保语言包加载完毕
  await setupI18n(app);
  // 获取默认语言
  const localLocale = localStorage.getItem('MS-locale');
  if (!localLocale) {
    const defaultLocale = await getDefaultLocale();
    const { changeLocale } = useLocale();
    changeLocale(defaultLocale);
  }

  app.use(router);
  app.use(ArcoVue);
  app.use(ArcoVueIcon);
  app.use(VueDOMPurifyHTML, {
    hooks: {
      afterSanitizeAttributes: (currentNode: Element) => {
        if ('target' in currentNode && 'rel' in currentNode) {
          const attribute = currentNode.getAttribute('target');
          currentNode.setAttribute('target', attribute == null ? '_blank' : attribute);
          currentNode.setAttribute('rel', 'noopener noreferrer nofollow');
        }
      },
    },
  });
  app.component('SvgIcon', SvgIcon);
  app.component('MsIcon', MsIcon);

  // 初始化本地存储
  localforage.config({
    driver: localforage.INDEXEDDB, // 选择后端存储，这里使用 IndexedDB
    name: 'MeterSphere', // 数据库名称
    version: 1.0, // 数据库版本
    storeName: 'msTable', // 存储空间名称
  });

  app.use(directive);

  app.mount('#app');
}

bootstrap();
