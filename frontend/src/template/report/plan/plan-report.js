import Vue from 'vue';
import ElementUI, {Button, Col, Form, FormItem, Input, Row} from 'element-ui';
import '@/assets/theme/index.css';
import '@/common/css/menu-header.css';
import '@/common/css/main.css';
import PlanReport from "./PlanReport.vue";
import i18n from "@/i18n/i18n";
import chart from "@/common/js/chart";


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

new Vue({
  el: '#planReport',
  i18n,
  render: h => h(PlanReport)
});
