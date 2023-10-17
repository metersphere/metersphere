import { Slots } from 'vue';

import { ArcoLang } from '@arco-design/web-vue/es/locale/interface';

export interface ArcoOptions {
  classPrefix?: string;
  componentPrefix?: string;
}
export const SIZES = ['mini', 'small', 'medium', 'large'] as const;

export type Size = (typeof SIZES)[number];

export interface ConfigProvider {
  slots: Slots;
  prefixCls?: string;
  locale?: ArcoLang;
  size?: Size;
  updateAtScroll?: boolean;
  scrollToClose?: boolean;
  exchangeTime?: boolean;
}
