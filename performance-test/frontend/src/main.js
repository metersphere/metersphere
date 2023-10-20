import "./public-path";
import Vue from "vue";
import "metersphere-frontend/src/styles/index.scss";
import ElementUI from "element-ui";
import App from "./App.vue";
import i18n from "./i18n";
import router, { microRouter } from "./router";
import { createPinia, PiniaVuePlugin } from "pinia";
import PersistedState from "pinia-plugin-persistedstate";
import icons from "metersphere-frontend/src/icons";
import svg from "metersphere-frontend/src/components/svg";
import plugins from "metersphere-frontend/src/plugins";
import directives from "metersphere-frontend/src/directive";
import filters from "metersphere-frontend/src/filters";
import chart from "metersphere-frontend/src/chart";
import "metersphere-frontend/src/router/permission";
import VueClipboard from "vue-clipboard2";
import VueShepherd from "vue-shepherd"; // 新手引导
import "metersphere-frontend/src/assets/shepherd/shepherd-theme.css";
import { gotoCancel, gotoNext } from "metersphere-frontend/src/utils";

Vue.config.productionTip = false;

const pinia = createPinia();
pinia.use(PersistedState); //开启缓存，存储在localstorage

Vue.use(ElementUI, {
  i18n: (key, value) => i18n.t(key, value),
  zIndex: 9000,
});

Vue.use(svg);
Vue.use(icons);
Vue.use(plugins);
Vue.use(directives);
Vue.use(filters);
Vue.use(chart);
Vue.use(PiniaVuePlugin);
Vue.use(VueClipboard);
Vue.use(VueShepherd);

Vue.prototype.gotoCancel = gotoCancel;
Vue.prototype.gotoNext = gotoNext;

let instance = null;

function render(props = {}) {
  const {
    container,
    eventBus = new Vue(),
    defaultPath,
    routeParams,
    routeName,
  } = props;
  // 添加全局事件总线
  Vue.prototype.$EventBus = eventBus;
  instance = new Vue({
    i18n,
    // 确定是否是内存路由
    router: defaultPath || routeName ? microRouter : router,
    pinia,
    render: (h) => h(App),
  }).$mount(container ? container.querySelector("#app") : "#app");
  // 解决qiankun下，vue-devtools不显示的问题
  if (process.env.NODE_ENV === "development") {
    const instanceDiv = document.createElement("div");
    instanceDiv.__vue__ = instance;
    document.body.appendChild(instanceDiv);
  }
  // 微服务过来的路由
  if (defaultPath || routeName) {
    microRouter.push({
      path: defaultPath,
      params: routeParams,
      name: routeName,
    });
  }
}

// 独立运行时
if (!window.__POWERED_BY_QIANKUN__) {
  render();
}

/**
 * bootstrap 只会在微应用初始化的时候调用一次，下次微应用重新进入时会直接调用 mount 钩子，不会再重复触发 bootstrap。
 * 通常我们可以在这里做一些全局变量的初始化，比如不会在 unmount 阶段被销毁的应用级别的缓存等。
 */
export async function bootstrap(props) {}

/**
 * 应用每次进入都会调用 mount 方法，通常我们在这里触发应用的渲染方法
 */
export async function mount(props) {
  props.onGlobalStateChange((state, prev) => {
    // state: 变更后的状态; prev 变更前的状态
  });
  props.setGlobalState({ event: "opendialog" });
  render(props);
}

/**
 * 应用每次 切出/卸载 会调用的方法，通常在这里我们会卸载微应用的应用实例
 */
export async function unmount(props) {
  instance.$destroy();
}

/**
 * 更新钩子，目前只有routeParams更新，后续有其他属性更新再添加
 */
export async function update(props) {
  const { defaultPath, routeParams, routeName } = props;
  // 微服务过来的路由
  if (defaultPath || routeName) {
    microRouter.push({
      path: defaultPath,
      params: routeParams,
      name: routeName,
    });
  }
}
