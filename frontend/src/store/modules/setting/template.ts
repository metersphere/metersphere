import { defineStore } from 'pinia';

import { isEnableTemplate } from '@/api/modules/setting/template';

import useAppStore from '../app';

const appStore = useAppStore();

const useTemplateStore = defineStore('template', {
  persist: true,
  state: (): {
    templateStatus: Record<string, boolean>;
  } => ({
    templateStatus: {
      FUNCTIONAL: true,
      API: true,
      UI: true,
      TEST_PLAN: true,
      BUG: true,
    },
  }),
  actions: {
    // 模板列表的状态
    async getStatus() {
      try {
        this.templateStatus = await isEnableTemplate(appStore.currentOrgId);
      } catch (error) {
        console.log(error);
      }
    },
    // 获取项目模板状态
    getProjectTemplateState() {
      const { FUNCTIONAL, API, UI, TEST_PLAN, BUG } = this.templateStatus;
      return {
        FUNCTIONAL: !FUNCTIONAL,
        API: !API,
        UI: !UI,
        TEST_PLAN: !TEST_PLAN,
        BUG: !BUG,
      } as Record<string, boolean>;
    },
    // 获取组织模板状态
    getOrdTemplateState() {
      return this.templateStatus;
    },
  },
});

export default useTemplateStore;
