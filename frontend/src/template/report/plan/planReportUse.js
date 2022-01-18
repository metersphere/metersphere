import Vue from 'vue';
import ElementUI, {Button, Col, Form, FormItem, Input, Row, Main, Card, Table, TableColumn, Popover} from 'element-ui';
import '@/assets/theme/index.css';
import '@/common/css/menu-header.css';
import '@/common/css/main.css';
import i18n from "@/i18n/i18n";
import chart from "@/common/js/chart";
import filters from "@/common/js/filter";
import icon from "@/common/js/icon";
import message from "@/common/js/message";
import mavonEditor from "mavon-editor";

// import CKEditor from '@ckeditor/ckeditor5-vue';

function planReportUse(id, template) {
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
  Vue.use(icon);
  Vue.use(message);
// Vue.use(CKEditor);
  Vue.use(mavonEditor);
  Vue.use(Popover);

  new Vue({
    el: id,
    i18n,
    render: h => h(template)
  });
}

export default planReportUse;
