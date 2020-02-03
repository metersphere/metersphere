import Vue from 'vue';
import VueI18n from "vue-i18n";
import enLocale from "element-ui/lib/locale/lang/en";
import zh_CNLocale from "element-ui/lib/locale/lang/zh-CN";
import en_US from "./en_US";
import zh_CN from "./zh_CN";

Vue.use(VueI18n);

const messages = {
  'en_US': {
    ...en_US,
    ...enLocale
  },
  'zh_CN': {
    ...zh_CN,
    ...zh_CNLocale
  }
};

const i18n = new VueI18n({
  locale: 'en_US',
  messages,
});

export default i18n;
