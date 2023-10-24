import { App } from 'vue';

import outerClick from './outerClick';
import permission from './permission';
import validateLicense from './validateLicense';

export default {
  install(Vue: App) {
    Vue.directive('permission', permission);
    Vue.directive('xpack', validateLicense);
    Vue.directive('outer', outerClick);
  },
};
