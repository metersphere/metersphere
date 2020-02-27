import Vue from 'vue';
import VueI18n from "vue-i18n";
import axios from "axios";
import enLocale from "element-ui/lib/locale/lang/en";
import zh_CNLocale from "element-ui/lib/locale/lang/zh-CN";
import zh_TWLocale from "element-ui/lib/locale/lang/zh-TW";
import zh_CN from "./zh-CN";

Vue.use(VueI18n);

const messages = {
  'en-US': {
    ...enLocale
  },
  'zh-CN': {
    ...zh_CN,
    ...zh_CNLocale
  },
  'zh-TW': {
    ...zh_TWLocale
  }
};

const i18n = new VueI18n({
  locale: 'zh-CN',
  messages,
});

const loadedLanguages = ['zh-CN'];

function setI18nLanguage(lang) {
  i18n.locale = lang;
  axios.defaults.headers.common['Accept-Language'] = lang;
  document.querySelector('html').setAttribute('lang', lang);
  return lang;
}

Vue.prototype.$setLang = function (lang) {
  if (i18n.locale !== lang) {
    if (!loadedLanguages.includes(lang)) {
      return import(`./${lang}`).then(response => {
        i18n.mergeLocaleMessage(lang, response.default);
        loadedLanguages.push(lang);
        return setI18nLanguage(lang)
      })
    }
    return Promise.resolve(setI18nLanguage(lang))
  }
  return Promise.resolve(lang)
};

export default i18n;
