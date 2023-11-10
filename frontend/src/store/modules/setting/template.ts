import { defineStore } from 'pinia';

import { getOrdTemplate, getProTemplate } from '@/api/modules/setting/template';

import useAppStore from '../app';

const appStore = useAppStore();

const useTemplateStore = defineStore('template', {
  persist: true,
  state: (): {
    ordStatus: Record<string, boolean>;
    projectStatus: Record<string, boolean>;
  } => ({
    ordStatus: {
      FUNCTIONAL: false,
      API: false,
      UI: false,
      TEST_PLAN: false,
      BUG: false,
    },
    projectStatus: {
      FUNCTIONAL: false,
      API: false,
      UI: false,
      TEST_PLAN: false,
      BUG: false,
    },
  }),
  actions: {
    // 模板列表的状态
    async getStatus() {
      const currentOrgId = computed(() => appStore.currentOrgId);
      const currentProjectId = computed(() => appStore.currentProjectId);
      try {
        this.ordStatus = await getOrdTemplate(currentOrgId.value);
        this.projectStatus = await getProTemplate(currentProjectId.value);
      } catch (error) {
        console.log(error);
      }
    },
  },
});

export default useTemplateStore;
