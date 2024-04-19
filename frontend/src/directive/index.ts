import { App } from 'vue';

import outerClick from './outerClick';
import permission from './permission';
import validateExpiration from './validateExpiration';
import validateLicense from './validateLicense';

export default {
  install(Vue: App) {
    Vue.directive('permission', permission);
    Vue.directive('xpack', validateLicense);
    Vue.directive('expire', validateExpiration);
    Vue.directive('outer', outerClick);
  },
};
