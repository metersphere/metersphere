import { unref, ref } from 'vue';
import dayjs from 'dayjs';
import { i18n } from '@/locale';
import { setHtmlPageLang, loadLocalePool } from '@/locale/helper';

import type { Recordable, LocaleType } from '#/global';

interface LangModule {
  message: Recordable;
  dayjsLocale: Recordable;
  dayjsLocaleName: string;
}

/**
 * 设置语言
 * @param locale 语言类型
 */
function setI18nLanguage(locale: LocaleType) {
  if (i18n.mode === 'legacy') {
    i18n.global.locale = locale;
  } else {
    (i18n.global.locale as any).value = locale;
  }
  localStorage.setItem('MS-locale', locale);
  setHtmlPageLang(locale);
}

/**
 * 切换语言
 * @param locale 语言类型
 * @returns 语言类型
 */
async function changeLocale(locale: LocaleType) {
  const globalI18n = i18n.global;
  const currentLocale = unref(globalI18n.locale);
  if (currentLocale === locale) {
    return locale;
  }

  if (loadLocalePool.includes(locale)) {
    setI18nLanguage(locale);
    return locale;
  }
  const langModule = ((await import(`./${locale}/index.ts`)) as any).default as LangModule;
  if (!langModule) return;

  const { message, dayjsLocale, dayjsLocaleName } = langModule;

  globalI18n.setLocaleMessage(locale, message);
  dayjs.locale(dayjsLocaleName, dayjsLocale);
  loadLocalePool.push(locale);

  setI18nLanguage(locale);
  return locale;
}

export default function useLocale() {
  const { locale } = i18n.global;
  const currentLocale = ref(locale);

  return {
    currentLocale,
    changeLocale,
  };
}
