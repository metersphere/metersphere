import Vue from 'vue';
import ElementUI, { Button, Col, Form, FormItem, Input, Row } from 'element-ui';
import 'metersphere-frontend/src/assets/theme/index.css';
import 'metersphere-frontend/src/styles/business/menu-header.css';
import 'metersphere-frontend/src/styles/business/main.css';
import directives from 'metersphere-frontend/src/directive';
import i18n from '@/i18n';
import filters from 'metersphere-frontend/src/filters';
import icons from 'metersphere-frontend/src/icons';
import plugins from 'metersphere-frontend/src/plugins';
import JsonSchemaEditor from '@/business/commons/json-schema/schema';
import JSONPathPicker from 'vue-jsonpath-picker';
import VuePapaParse from 'vue-papa-parse';
import mavonEditor from 'mavon-editor';

function documentUse(id, template) {
  Vue.use(ElementUI, {
    i18n: (key, value) => i18n.t(key, value),
  });
  Vue.use(Row);
  Vue.use(Col);
  Vue.use(Form);
  Vue.use(FormItem);
  Vue.use(Input);
  Vue.use(Button);
  Vue.use(JSONPathPicker);
  Vue.use(JsonSchemaEditor);
  Vue.use(VuePapaParse);
  Vue.use(mavonEditor);
  Vue.use(filters);
  Vue.use(directives);
  Vue.use(icons);
  Vue.use(plugins);

  setTimeout(() => {
    new Vue({
      el: id,
      i18n,
      render: (h) => h(template),
    });
    // 不延迟页面渲染不出来
  }, 5000);
}

export default documentUse;
