import { debounce } from 'lodash-es';

import type { PiniaPluginContext } from 'pinia';
// 首先得声明插件使用到的额外属性，因为pinia的TS类型声明中，每个store只有三个原生属性state、getters、actions，若没有使用到额外属性则无需声明
declare module 'pinia' {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  export interface DefineStoreOptionsBase<S, Store> {
    debounce?: Partial<Record<keyof StoreActions<Store>, number>>; // 节流配置
  }
}
// 基于lodash的防抖函数封装，读取store中cache配置的属性，针对已配置的属性更改操作进行防抖处理
export const debouncePlugin = ({ options, store }: PiniaPluginContext): void | Record<string, any> => {
  if (options.debounce) {
    return Object.keys(options.debounce).reduce((debounceActions: Record<string, any>, action) => {
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      debounceActions[action] = debounce(store[action], options.debounce![action]);
      return debounceActions;
    }, {});
  }
};
