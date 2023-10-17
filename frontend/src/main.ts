/* eslint-disable simple-import-sort/imports */
import { createApp } from 'vue';
import ArcoVue from '@arco-design/web-vue';
import '@arco-themes/vue-ms-theme-default/index.less';

import MsIcon from '@/components/pure/ms-icon-font/index.vue';
import SvgIcon from '@/components/pure/svg-icon/index.vue';
import App from './App.vue';

// eslint-disable-next-line import/no-unresolved
import 'virtual:svg-icons-register';
import directive from './directive';
import { setupI18n } from './locale';
import router from './router';
import store from './store';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';
import '@/assets/style/global.less';

async function bootstrap() {
  const app = createApp(App);

  app.use(store);
  // 注册国际化，需要异步阻塞，确保语言包加载完毕
  await setupI18n(app);
  app.use(router);
  app.use(ArcoVue);
  app.use(ArcoVueIcon);
  app.component('SvgIcon', SvgIcon);
  app.component('MsIcon', MsIcon);

  app.use(directive);

  app.mount('#app');
}

bootstrap();
