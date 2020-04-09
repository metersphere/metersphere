import Vue from 'vue';
import ElementUI, {Button, Col, Form, FormItem, Input, Row} from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import Login from "./Login.vue";
import Ajax from "../common/js/ajax";
import i18n from "../i18n/i18n";

Vue.config.productionTip = false;

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


new Vue({
  el: '#login',
  i18n,
  render: h => h(Login)
});
