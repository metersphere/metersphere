import { defineStore } from 'pinia';
import { cloneDeep } from 'lodash-es';

import type { RequestCompositionState } from './types';

// 用于记录请求组合的临时插件表单数据
const useRequestCompositionStore = defineStore('pluginForm', {
  state: (): RequestCompositionState => ({
    temporaryPluginFormMap: {},
  }),
  getters: {
    pluginFormMap: (state) => state.temporaryPluginFormMap,
  },
  actions: {
    setPluginFormMap(id: string | number, pluginForm?: Record<string, any>) {
      this.temporaryPluginFormMap[id] = pluginForm ? cloneDeep(pluginForm) : {};
    },
    removePluginFormMapItem(id: string | number) {
      if (this.temporaryPluginFormMap[id]) {
        delete this.temporaryPluginFormMap[id];
      }
    },
  },
});

export default useRequestCompositionStore;
