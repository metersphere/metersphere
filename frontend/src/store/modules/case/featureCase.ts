import { defineStore } from 'pinia';

import { getCaseModulesCounts, getRecycleModulesCounts } from '@/api/modules/case-management/featureCase';

import type { CaseModuleQueryParams, TabItemType } from '@/models/caseManagement/featureCase';
import { ModuleTreeNode } from '@/models/projectManagement/file';

const useFeatureCaseStore = defineStore('featureCase', {
  persist: true,
  state: (): {
    moduleId: string[]; // 当前选中模块
    allModuleId: string[]; // 所有模块
    caseTree: ModuleTreeNode[]; // 用例树
    modulesCount: Record<string, any>; // 用例树模块数量
    recycleModulesCount: Record<string, any>; // 回收站模块数量
    operatingState: boolean; // 操作状态
    tabSettingList: TabItemType[]; // 详情tab
  } => ({
    moduleId: [],
    allModuleId: [],
    caseTree: [],
    modulesCount: {},
    recycleModulesCount: {},
    operatingState: false,
    tabSettingList: [],
  }),
  actions: {
    // 设置选择moduleId
    setModuleId(currentModuleId: string[], offspringIds: string[]) {
      this.moduleId = currentModuleId;
      if (offspringIds.length > 0) {
        this.allModuleId = offspringIds;
      }
    },
    // 设置用例树
    setModulesTree(tree: ModuleTreeNode[]) {
      this.caseTree = tree;
    },
    // 获取模块数量
    async getCaseModulesCount(params: CaseModuleQueryParams) {
      try {
        this.modulesCount = {};
        this.modulesCount = await getCaseModulesCounts(params);
      } catch (error) {
        console.log(error);
      }
    },
    // 获取模块数量
    async getRecycleModulesCount(params: CaseModuleQueryParams) {
      try {
        this.recycleModulesCount = {};
        this.recycleModulesCount = await getRecycleModulesCounts(params);
      } catch (error) {
        console.log(error);
      }
    },
    // 设置是否是编辑或者新增成功状态
    setIsAlreadySuccess(state: boolean) {
      this.operatingState = state;
    },
    // 设置菜单
    setTab(list: TabItemType[]) {
      this.tabSettingList = list;
    },
    // 获取显示的tab
    getTab() {
      return this.tabSettingList.filter((item) => item.enable);
    },
  },
});

export default useFeatureCaseStore;
