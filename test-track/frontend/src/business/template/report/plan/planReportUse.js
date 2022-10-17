import Vue from 'vue';
import "@/business/utils/track-table-header";
import ElementUI, {Button, Col, Form, FormItem, Input, Row, Main, Card, Table, TableColumn, Popover} from 'element-ui';
import 'metersphere-frontend/src/assets/theme/index.css';
import 'metersphere-frontend/src/styles/business/menu-header.css';
import 'metersphere-frontend/src/styles/business/main.css';
import i18n from "@/i18n";
import chart from "metersphere-frontend/src/chart";
import filters from "metersphere-frontend/src/filters";
import icons from "metersphere-frontend/src/icons"
import plugins from "metersphere-frontend/src/plugins";
import mavonEditor from "mavon-editor";
import {createPinia, PiniaVuePlugin} from 'pinia'
import PersistedState from "pinia-plugin-persistedstate";
import {TokenKey} from "metersphere-frontend/src/utils/constants";
import axios from 'axios';

function planReportUse(id, template) {
  // 获取gateway路由
  let user = JSON.parse(localStorage.getItem(TokenKey));
  axios({
    method: 'get',
    url: '/services',
    headers: {
      'CSRF-TOKEN': user.csrfToken,
      'X-AUTH-TOKEN': user.sessionId
    },
  }).then((res)=>{
    let modules = {}, microPorts = {};
    res.data.data.forEach(svc => {
      let name = svc.serviceId;
      modules[name] = true;
      microPorts[name] = svc.port;
    })
    sessionStorage.setItem("micro_apps", JSON.stringify(modules));
    sessionStorage.setItem("micro_ports", JSON.stringify(microPorts));
  })

  const pinia = createPinia()
  pinia.use(PersistedState)//开启缓存，存储在localstorage

  Vue.use(ElementUI, {
    i18n: (key, value) => i18n.t(key, value)
  });

  Vue.use(Row);
  Vue.use(Col);
  Vue.use(Form);
  Vue.use(FormItem);
  Vue.use(Input);
  Vue.use(Button);
  Vue.use(chart);
  Vue.use(Main);
  Vue.use(Card);
  Vue.use(TableColumn);
  Vue.use(Table);
  Vue.use(filters);
  Vue.use(icons);
  Vue.use(plugins);
  Vue.use(mavonEditor);
  Vue.use(Popover);
  Vue.use(PiniaVuePlugin);
  Vue.use(pinia);

  setTimeout(() => {
    new Vue({
      el: id,
      i18n,
      pinia,
      render: h => h(template)
    });
    // 不延迟页面渲染不出来
  }, 500);

}

export default planReportUse;
