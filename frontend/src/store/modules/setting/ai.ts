import { defineStore } from 'pinia';

import { OptionItem } from '@/api/modules/message/index';
import { getModelConfigNameList } from '@/api/modules/setting/config';
import { hasAnyPermission } from '@/utils/permission';

const useAIStore = defineStore('ai', {
  state: (): {
    aiSourceNameList: OptionItem[];
  } => ({
    aiSourceNameList: [],
  }),
  actions: {
    async getAISourceNameList() {
      if (!hasAnyPermission(['SYSTEM_PARAMETER_SETTING_AI_MODEL:READ'])) return;
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
