import Vue from 'vue';
import ElementUI from 'element-ui';
import '../assets/theme/index.css';
import icon from "../common/js/icon";
import filters from "../common/js/filter";
import ajax from "../common/js/ajax";
import App from './App.vue';
import message from "../common/js/message";
import router from "./components/common/router/router";
import YanProgress from 'yan-progress';
import './permission' // permission control
import i18n from "../i18n/i18n";
import store from "./store";
import {permission, roles, tester, xpack} from './permission'
import chart from "../common/js/chart";
import CalendarHeatmap from "../common/js/calendar-heatmap";
import '../common/css/menu-header.css';
import '../common/css/main.css';
import CKEditor from '@ckeditor/ckeditor5-vue';
import VueFab from 'vue-float-action-button'
import {horizontalDrag} from "../common/js/directive";
import JsonSchemaEditor from './components/common/json-schema/schema/index';
Vue.use(JsonSchemaEditor);

Vue.config.productionTip = false;
Vue.use(icon);
Vue.use(ElementUI, {
  i18n: (key, value) => i18n.t(key, value)
});
Vue.use(filters);
Vue.use(ajax);
Vue.use(chart);
Vue.use(CalendarHeatmap);
Vue.use(message);
Vue.use(CKEditor);
Vue.use(YanProgress);
Vue.use(VueFab);

// v-permission
Vue.directive('permission', permission);

// v-roles
Vue.directive('roles', roles);

Vue.directive('xpack', xpack);

Vue.directive('tester', tester);

//支持左右拖拽
Vue.directive('horizontal-drag', horizontalDrag);

new Vue({
  el: '#app',
  router,
  store,
  i18n,
  render: h => h(App)
});
