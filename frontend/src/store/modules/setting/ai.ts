import { defineStore } from 'pinia';

import { OptionItem } from '@/api/modules/message/index';
import { getModelConfigNameList } from '@/api/modules/setting/config';

const useAIStore = defineStore('ai', {
  state: (): {
    aiSourceNameList: OptionItem[];
  } => ({
    aiSourceNameList: [],
  }),
  actions: {
    async getAISourceNameList() {
      try {
        this.aiSourceNameList = (await getModelConfigNameList()) || [];
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
  },
});

export default useAIStore;
