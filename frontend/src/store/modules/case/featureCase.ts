import { defineStore } from 'pinia';

import { getCaseModulesCounts, getRecycleModulesCounts } from '@/api/modules/case-management/featureCase';

import type { CaseModuleQueryParams, TabItemType } from '@/models/caseManagement/featureCase';
import { ModuleTreeNode } from '@/models/common';

const useFeatureCaseStore = defineStore('featureCase', {
  persist: true,
  state: (): {
    moduleId: string[]; // 当前选中模块
    caseTree: ModuleTreeNode[]; // 用例树
    modulesCount: Record<string, any>; // 用例树模块数量
    recycleModulesCount: Record<string, any>; // 回收站模块数量
    operatingState: boolean; // 操作状态
    tabSettingList: TabItemType[]; // 详情tab
    activeTab: string; // 激活tab
  } => ({
    moduleId: [],
    caseTree: [],
    modulesCount: {},
    recycleModulesCount: {},
    operatingState: false,
    tabSettingList: [],
    activeTab: 'detail',
  }),
  actions: {
    // 设置选择moduleId
    setModuleId(currentModuleId: string[]) {
      if (['all', 'recycle'].includes(currentModuleId[0])) {
        this.moduleId = ['root'];
      } else {
        this.moduleId = currentModuleId;
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
    // 设置菜单
    setTab(list: TabItemType[]) {
      this.tabSettingList = list;
    },
    // 获取显示的tab
    getTab() {
      return this.tabSettingList.filter((item) => item.enable);
    },
    // 设置激活tab
    setActiveTab(active: string | number) {
      this.activeTab = active as string;
    },
    // 设置菜单模块列表数量
    setListCount(type: string, count = 0) {
      this.tabSettingList = this.tabSettingList.map((item: any) => {
        if (type === item.key) {
          return {
            ...item,
            total: count,
          };
        }
        return {
          ...item,
        };
      });
    },
    // 初始化count
    initCountMap(countMap: Record<string, any>) {
      this.tabSettingList = this.tabSettingList.map((item) => {
        return {
          ...item,
          total: countMap[item.key] || 0,
        };
      });
    },
  },
});

export default useFeatureCaseStore;
