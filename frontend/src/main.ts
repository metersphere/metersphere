import { createApp } from 'vue';
import FormCreate from '@form-create/arco-design';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';
import SvgIcon from '@/components/svg-icon/index.vue';
import Breadcrumb from '@/components/breadcrumb/index.vue';
import router from './router';
import store from './store';
import { setupI18n } from './locale';
import directive from './directive';
import './mock';
import App from './App.vue';
// eslint-disable-next-line import/no-unresolved
import 'virtual:svg-icons-register';
// Styles are imported via arco-plugin. See config/plugin/arcoStyleImport.ts in the directory for details
// 样式通过 arco-plugin 插件导入。详见目录文件 config/plugin/arcoStyleImport.ts
// https://arco.design/docs/designlab/use-theme-package
import '@/assets/style/global.less';

async function bootstrap() {
  const app = createApp(App);

  app.use(store);
  // 注册国际化，需要异步阻塞，确保语言包加载完毕
  await setupI18n(app);

  app.use(ArcoVueIcon);
  app.component('SvgIcon', SvgIcon);
  app.component('Breadcrumb', Breadcrumb);

  app.use(router);
  app.use(directive);
  app.use(FormCreate);

  app.mount('#app');
}

bootstrap();
