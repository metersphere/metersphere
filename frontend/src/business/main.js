import Vue from 'vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import icon from "../common/js/icon";
import filters from "../common/js/filter";
import ajax from "../common/js/ajax";
import App from './App.vue';
import router from "./components/common/router/router";
import './permission' // permission control
import i18n from "../i18n/i18n";
import store from "./store";
import {permission} from './permission'
import chart from "../common/js/chart";
import '../common/css/menu-header.css';
import '../common/css/main.css';

Vue.config.productionTip = false;
Vue.use(icon);
Vue.use(ElementUI, {
  i18n: (key, value) => i18n.t(key, value)
});
Vue.use(filters);
Vue.use(ajax);
Vue.use(chart);

// v-permission
Vue.directive('permission', permission);

new Vue({
  el: '#app',
  router,
  store,
  i18n,
  render: h => h(App)
});
