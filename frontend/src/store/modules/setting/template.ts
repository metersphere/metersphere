import { defineStore } from 'pinia';

import { getOrdTemplate, getProTemplate } from '@/api/modules/setting/template';
import { hasAnyPermission } from '@/utils/permission';

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
        if (currentOrgId.value && hasAnyPermission(['ORGANIZATION_TEMPLATE:READ'])) {
          this.ordStatus = await getOrdTemplate(currentOrgId.value);
        }
        if (
          currentProjectId.value &&
          hasAnyPermission(['PROJECT_TEMPLATE:READ']) &&
          currentProjectId.value !== 'no_such_project'
        ) {
          this.projectStatus = await getProTemplate(currentProjectId.value);
        }
      } catch (error) {
        console.log(error);
      }
    },
  },
});

export default useTemplateStore;
