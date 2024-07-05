import type { LocaleType } from '#/global';

export function setHtmlPageLang(locale: LocaleType) {
  document.querySelector('html')?.setAttribute('lang', locale);
  document.body.style.fontFamily = 'PingFang SC, AlibabaPuHuiTi';
}

export const loadLocalePool: LocaleType[] = [];

export function setLoadLocalePool(cb: (lp: LocaleType[]) => void) {
  cb(loadLocalePool);
}
