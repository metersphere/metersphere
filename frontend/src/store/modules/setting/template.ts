import { defineStore } from 'pinia';

import { isEnableTemplate } from '@/api/modules/setting/template';

import useAppStore from '../app';

const useTemplateStore = defineStore('template', {
  persist: true,
  state: (): { templateStatus: Record<string, boolean> } => ({
    templateStatus: {
      FUNCTIONAL: false,
      API: false,
      UI: false,
      TEST_PLAN: false,
      BUG: false,
    },
  }),
  actions: {
    setStatus() {
      const appStore = useAppStore();
      Object.keys(this.templateStatus).forEach(async (item) => {
        const sceneStatus = await isEnableTemplate(appStore.currentOrgId, item);
        this.templateStatus[item] = sceneStatus;
      });
    },
  },
});

export default useTemplateStore;
