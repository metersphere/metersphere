import Vue from "vue";
import "@/styles/index.scss";
import ElementUI from "element-ui";
import App from "./App.vue";
import i18n from "./i18n";
import router from "./router/index";
import {createPinia, PiniaVuePlugin} from 'pinia';
import PersistedState from 'pinia-plugin-persistedstate';
import icons from "./icons";
import svg from "./components/svg";
import plugins from "./plugins";
import directives from "./directive";
import filters from "./filters";
import "./router/permission";
import "./micro-app";
import mavonEditor from 'mavon-editor';
import 'mavon-editor/dist/css/index.css';

Vue.config.productionTip = false

const pinia = createPinia()
pinia.use(PersistedState)//开启缓存，存储在localstorage

Vue.use(ElementUI, {
  i18n: (key, value) => i18n.t(key, value)
});


Vue.use(icons);
Vue.use(svg);
Vue.use(plugins);
Vue.use(directives);
Vue.use(filters);
Vue.use(PiniaVuePlugin);
Vue.use(mavonEditor);

new Vue({
  el: "#app",
  i18n,
  router,
  pinia,
  render: h => h(App),
})
