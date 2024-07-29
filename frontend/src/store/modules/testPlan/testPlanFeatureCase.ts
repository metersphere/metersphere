import { defineStore } from 'pinia';

import { getFeatureCaseModule, getFeatureCaseModuleCount } from '@/api/modules/test-plan/testPlan';
import { mapTree } from '@/utils';

import { ModuleTreeNode } from '@/models/common';
import type { PlanDetailFeatureCaseListQueryParams } from '@/models/testPlan/testPlan';

const useTestPlanFeatureCaseStore = defineStore('testPlanFeatureCase', {
  state: (): {
    modulesCount: Record<string, any>; // 用例树模块数量
    moduleTree: ModuleTreeNode[]; // 用例树
    loading: boolean;
  } => ({
    modulesCount: {},
    moduleTree: [],
    loading: false,
  }),
  actions: {
    // 初始化模块树
    async initModules(id: string, treeType: 'MODULE' | 'COLLECTION') {
      try {
        this.loading = true;
        const res = await getFeatureCaseModule({ testPlanId: id, treeType });
        this.moduleTree = mapTree<ModuleTreeNode>(res, (node) => {
          return {
            ...node,
            count: this.modulesCount?.[node.id] || 0,
          };
        });
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        this.loading = false;
      }
    },
    // 设置模块树
    setModulesTree(tree: ModuleTreeNode[]) {
      this.moduleTree = tree;
    },
    async getModuleCount(params: PlanDetailFeatureCaseListQueryParams) {
      try {
        this.modulesCount = await getFeatureCaseModuleCount(params);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
  },
});

export default useTestPlanFeatureCaseStore;
