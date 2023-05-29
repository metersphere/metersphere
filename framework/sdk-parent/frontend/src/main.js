import Vue from "vue";
import "@/styles/index.scss";
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
import VueShepherd from 'vue-shepherd' // 新手引导
import './assets/shepherd/shepherd-theme.css'
import { gotoCancel, gotoNext } from "./utils";

Vue.config.productionTip = false

const pinia = createPinia()
pinia.use(PersistedState)//开启缓存，存储在localstorage

Vue.use(icons);
Vue.use(svg);
Vue.use(plugins);
Vue.use(directives);
Vue.use(filters);
Vue.use(PiniaVuePlugin);
Vue.use(VueShepherd)

Vue.prototype.gotoCancel = gotoCancel
Vue.prototype.gotoNext = gotoNext

new Vue({
  el: "#app",
  i18n,
  router,
  pinia,
  render: h => h(App),
})
