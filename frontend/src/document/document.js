import Vue from 'vue';
import ElementUI, {Button, Col, Form, FormItem, Input, Row} from 'element-ui';
import '../assets/theme/index.css';
import '../common/css/menu-header.css';
import '../common/css/main.css';

import Document from "./Document.vue";
import Ajax from "@/common/js/ajax";
import i18n from "@/i18n/i18n";
import router from "@/business/components/common/router/router";
import JsonSchemaEditor from "@/business/components/common/json-schema/schema/index";
import JSONPathPicker from 'vue-jsonpath-picker';
Vue.use(JsonSchemaEditor);
import VuePapaParse from 'vue-papa-parse'
Vue.use(VuePapaParse)

Vue.use(ElementUI, {
  i18n: (key, value) => i18n.t(key, value)
});
Vue.use(Row);
Vue.use(Col);
Vue.use(Form);
Vue.use(FormItem);
Vue.use(Input);
Vue.use(Button);
Vue.use(Ajax);
Vue.use(JSONPathPicker);

new Vue({
  el: '#document',
  router,
  i18n,
  render: h => h(Document)
});
