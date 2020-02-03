import Vue from 'vue';
import {Button, Col, Form, FormItem, Input, Row} from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import Login from "./Login.vue";
import Ajax from "../common/ajax";

Vue.config.productionTip = false;
Vue.use(Row);
Vue.use(Col);
Vue.use(Form);
Vue.use(FormItem);
Vue.use(Input);
Vue.use(Button);
Vue.use(Ajax);

new Vue({
  el: '#login',
  render: h => h(Login)
});
