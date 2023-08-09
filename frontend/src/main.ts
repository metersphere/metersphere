import { createApp } from 'vue';
import ArcoVue from '@arco-design/web-vue';
import '@arco-themes/vue-ms-theme-default/index.less';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';
import SvgIcon from '@/components/pure/svg-icon/index.vue';
import MSIcon from '@/components/pure/ms-icon-font/index.vue';
import router from './router';
import store from './store';
import { setupI18n } from './locale';
import directive from './directive';
import './mock';
import App from './App.vue';
// eslint-disable-next-line import/no-unresolved
import 'virtual:svg-icons-register';
import '@/assets/style/global.less';

async function bootstrap() {
  const app = createApp(App);

  app.use(store);
  // 注册国际化，需要异步阻塞，确保语言包加载完毕
  await setupI18n(app);
  app.use(ArcoVue);
  app.use(ArcoVueIcon);
  app.component('MsIcon', MSIcon);
  app.component('SvgIcon', SvgIcon);

  app.use(router);
  app.use(directive);

  app.mount('#app');
}

bootstrap();
