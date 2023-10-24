import { defineStore } from 'pinia';

import { isEnableTemplate } from '@/api/modules/setting/template';

import type { DefinedFieldItem } from '@/models/setting/template';

import useAppStore from '../app';

const useTemplateStore = defineStore('template', {
  persist: true,
  state: (): { templateStatus: Record<string, boolean>; previewList: DefinedFieldItem[] } => ({
    templateStatus: {
      FUNCTIONAL: false,
      API: false,
      UI: false,
      TEST_PLAN: false,
      BUG: false,
    },
    previewList: [],
  }),
  actions: {
    // 模板列表的状态
    setStatus() {
      // 需要调整接口
      // const appStore = useAppStore();
      // Object.keys(this.templateStatus).forEach(async (item) => {
      //   const sceneStatus = await isEnableTemplate(appStore.currentOrgId, item);
      //   this.templateStatus[item] = sceneStatus;
      // });
    },
    // 预览存储表数据
    setPreviewHandler(filedData: DefinedFieldItem[]) {
      this.previewList = filedData;
    },
  },
});

export default useTemplateStore;
