import { getCurrentInstance, inject, InjectionKey } from 'vue';

import type { ArcoOptions, ConfigProvider } from './types';
import type { App } from 'vue';

const COMPONENT_PREFIX = 'A';
const CLASS_PREFIX = 'ms';
const GLOBAL_CONFIG_NAME = '$arco';

export const configProviderInjectionKey: InjectionKey<ConfigProvider> = Symbol('ArcoConfigProvider');

export const getComponentPrefix = (options?: ArcoOptions) => {
  return options?.componentPrefix ?? COMPONENT_PREFIX;
};

export const setGlobalConfig = (app: App, options?: ArcoOptions): void => {
  if (options && options.classPrefix) {
    app.config.globalProperties[GLOBAL_CONFIG_NAME] = {
      ...(app.config.globalProperties[GLOBAL_CONFIG_NAME] ?? {}),
      classPrefix: options.classPrefix,
    };
  }
};

export const getPrefixCls = (componentName?: string): string => {
  const instance = getCurrentInstance();
  const configProvider = inject(configProviderInjectionKey, undefined);

  const prefix =
    configProvider?.prefixCls ??
    instance?.appContext.config.globalProperties[GLOBAL_CONFIG_NAME]?.classPrefix ??
    CLASS_PREFIX;
  if (componentName) {
    return `${prefix}-${componentName}`;
  }
  return prefix;
};

export const getLegalPage = (page: number, { min, max }: { min: number; max: number }): number => {
  if (page < min) {
    return min;
  }
  if (page > max) {
    return max;
  }
  return page;
};

export function isNumber(obj: any): obj is number {
  return Object.prototype.toString.call(obj) === '[object Number]' && obj === obj; // eslint-disable-line
}
