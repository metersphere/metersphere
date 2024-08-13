import { ref, unref } from 'vue';
import { Message } from '@arco-design/web-vue';
import dayjs from 'dayjs';

import { i18n } from '@/locale';
import { loadLocalePool } from '@/locale/helper';

import type { LocaleType, Recordable } from '#/global';

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
    setI18nLanguage(locale); // 初始化的时候需要设置一次本地语言
    return locale;
  }
  Message.loading(currentLocale === 'zh-CN' ? '语言切换中...' : 'Language switching...');

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
  window.location.reload();
  return locale;
}

export default function useLocale() {
  const { locale } = i18n.global;
  const currentLocale = ref(locale as LocaleType);

  return {
    currentLocale,
    changeLocale,
  };
}
