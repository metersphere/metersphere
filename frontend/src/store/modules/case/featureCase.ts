import { defineStore } from 'pinia';

import {
  getCaseDefaultFields,
  getCaseDetail,
  getCaseModulesCounts,
  getRecycleModulesCounts,
} from '@/api/modules/case-management/featureCase';
import useLocalForage from '@/hooks/useLocalForage';
import { isArraysEqualWithOrder } from '@/utils/equal';

import type { ContentTabsMap, CustomAttributes, TabItemType } from '@/models/caseManagement/featureCase';
import { ModuleTreeNode, TableQueryParams } from '@/models/common';

import useAppStore from '../app';

const useFeatureCaseStore = defineStore('featureCase', {
  persist: true,
  state: (): {
    moduleId: string[]; // 当前选中模块
    caseTree: ModuleTreeNode[]; // 用例树
    modulesCount: Record<string, any>; // 用例树模块数量
    recycleModulesCount: Record<string, any>; // 回收站模块数量
    operatingState: boolean; // 操作状态
    countMap: Record<string, any>;
    activeTab: string; // 激活tab
    defaultFields: CustomAttributes[];
    defaultCount: Record<string, any>;
  } => ({
    moduleId: [],
    caseTree: [],
    modulesCount: {},
    recycleModulesCount: {},
    operatingState: false,
    activeTab: 'detail',
    defaultFields: [],
    defaultCount: {},
    countMap: {
      case: '0',
      dependency: '0',
      caseReview: '0',
      testPlan: '0',
      bug: '0',
      requirement: '0',
      comments: '0',
      changeHistory: '0',
    },
  }),
  actions: {
    // 设置选择moduleId
    setModuleId(currentModuleId: string[]) {
      if (['all', 'recycle'].includes(currentModuleId[0])) {
        this.moduleId = [];
      } else {
        this.moduleId = currentModuleId;
      }
    },
    // 设置用例树
    setModulesTree(tree: ModuleTreeNode[]) {
      this.caseTree = tree;
    },
    // 获取模块数量
    async getCaseModulesCount(params: TableQueryParams) {
      try {
        this.modulesCount = await getCaseModulesCounts(params);
      } catch (error) {
        console.log(error);
      }
    },
    // 获取模块数量
    async getRecycleModulesCount(params: TableQueryParams) {
      try {
        this.recycleModulesCount = await getRecycleModulesCounts(params);
      } catch (error) {
        console.log(error);
      }
    },
    // 设置激活tab
    setActiveTab(active: string | number) {
      this.activeTab = active as string;
    },
    // 设置菜单模块列表数量
    setListCount(type: string, count = 0) {
      this.countMap[type] = count;
    },
    // 初始化count
    initCountMap(countMap: Record<string, any>) {
      this.countMap = { ...countMap };
    },
    // 获取默认模版
    async getDefaultTemplate() {
      try {
        const appStore = useAppStore();
        const result = await getCaseDefaultFields(appStore.currentProjectId);
        this.defaultFields = result.customFields;
      } catch (error) {
        console.log(error);
      }
    },
    // 获取系统字段用例等级
    getSystemCaseLevelFields() {
      return (
        this.defaultFields.find((item: any) => item.internal && item.internalFieldKey === 'functional_priority')
          ?.options || []
      );
    },
    // 获取详情
    async getCaseCounts(caseId: string) {
      try {
        const result = await getCaseDetail(caseId);
        const {
          bugCount,
          caseCount,
          caseReviewCount,
          demandCount,
          relateEdgeCount,
          testPlanCount,
          historyCount,
          commentCount,
        } = result;
        const countMap: Record<string, any> = {
          case: caseCount,
          dependency: relateEdgeCount,
          caseReview: caseReviewCount,
          testPlan: testPlanCount,
          bug: bugCount,
          requirement: demandCount,
          changeHistory: historyCount,
          comments: commentCount,
        };
        this.initCountMap(countMap);
      } catch (error) {
        console.log(error);
      }
    },
    async initContentTabList(arr: TabItemType[]) {
      try {
        const { getItem, setItem } = useLocalForage();
        const tabsMap = await getItem<ContentTabsMap>('caseTabsMap');
        if (tabsMap) {
          const { backupTabList } = tabsMap;
          const isEqual = isArraysEqualWithOrder<TabItemType>(backupTabList, arr);
          if (!isEqual) {
            tabsMap.tabList = arr;
            tabsMap.backupTabList = arr;
            await setItem('caseTabsMap', tabsMap);
          }
        } else {
          await setItem('caseTabsMap', { tabList: arr, backupTabList: arr });
        }
      } catch (e) {
        console.log(e);
      }
    },
    async getContentTabList() {
      try {
        const { getItem } = useLocalForage();
        const tabsMap = await getItem<ContentTabsMap>('caseTabsMap');
        if (tabsMap) {
          return tabsMap.tabList;
        }
        return [];
      } catch (e) {
        console.log(e);
      }
    },
    async setContentTabList(arr: TabItemType[]) {
      const { getItem, setItem } = useLocalForage();
      const tabsMap = await getItem<ContentTabsMap>('caseTabsMap');
      if (tabsMap) {
        const tmpArrList = JSON.parse(JSON.stringify(arr));
        tabsMap.tabList = tmpArrList;
        await setItem('caseTabsMap', tabsMap);
      }
    },
  },
});

export default useFeatureCaseStore;
