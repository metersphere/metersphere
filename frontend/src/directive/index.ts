import { App } from 'vue';
import permission from './permission';

export default {
  install(Vue: App) {
    Vue.directive('permission', permission);
  },
};
