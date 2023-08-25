import Vue from 'vue';
import ElementUI, {
  Button,
  Card,
  Col,
  Form,
  FormItem,
  Input,
  Main,
  Popover,
  Row,
  Table,
  TableColumn,
} from 'element-ui';
import 'metersphere-frontend/src/assets/theme/index.css';
import 'metersphere-frontend/src/styles/business/menu-header.css';
import 'metersphere-frontend/src/styles/business/main.css';
import directives from 'metersphere-frontend/src/directive';
import i18n from '@/i18n';
import chart from 'metersphere-frontend/src/chart';
import filters from 'metersphere-frontend/src/filters';
import icons from 'metersphere-frontend/src/icons';
import plugins from 'metersphere-frontend/src/plugins';
import VueVirtualTree from '@fit2cloud-ui/vue-virtual-tree';
function apiReportUse(id, template) {
  Vue.use(ElementUI, {
    i18n: (key, value) => i18n.t(key, value),
  });

  Vue.use(VueVirtualTree);
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
  Vue.use(Popover);
  Vue.use(directives);
  Vue.use(icons);
  Vue.use(plugins);

  new Vue({
    el: id,
    i18n,
    render: (h) => h(template),
  });
}

export default apiReportUse;
