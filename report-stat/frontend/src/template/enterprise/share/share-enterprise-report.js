import ShareEnterpriseReportTemplate from "./ShareEnterpriseReportTemplate";

import Vue from 'vue';
import ElementUI, {Button, Card, Col, Form, FormItem, Input, Main, Row, Table, TableColumn} from 'element-ui';
import 'metersphere-frontend/src/assets/theme/index.css';
import 'metersphere-frontend/src/styles/business/menu-header.css';
import 'metersphere-frontend/src/styles/business/main.css';
import directives from "metersphere-frontend/src/directive";
import i18n from "@/i18n";
import filters from "metersphere-frontend/src/filters";
import icons from "metersphere-frontend/src/icons";
import plugins from "metersphere-frontend/src/plugins";
import JSONPathPicker from 'vue-jsonpath-picker';
import VuePapaParse from 'vue-papa-parse'
import mavonEditor from 'mavon-editor'
import chart from "metersphere-frontend/src/chart";

Vue.use(ElementUI, {
  i18n: (key, value) => i18n.t(key, value)
});
Vue.use(Row);
Vue.use(Col);
Vue.use(Form);
Vue.use(FormItem);
Vue.use(Input);
Vue.use(Button);
Vue.use(JSONPathPicker);
Vue.use(VuePapaParse);
Vue.use(mavonEditor);
Vue.use(filters);
Vue.use(directives);
Vue.use(icons);
Vue.use(plugins);
Vue.use(chart);
Vue.use(Main);
Vue.use(Card);
Vue.use(TableColumn);
Vue.use(Table);
Vue.use(filters);


new Vue({
  el: '#shareEnterpriseReport',
  i18n,
  render: h => h(ShareEnterpriseReportTemplate)
});

