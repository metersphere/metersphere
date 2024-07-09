import { defineStore } from 'pinia';

import { getReviewDetailModuleCount, getReviewDetailModuleTree } from '@/api/modules/case-management/caseReview';
import { mapTree } from '@/utils';

import type { ReviewDetailCaseListQueryParams } from '@/models/caseManagement/caseReview';
import { ModuleTreeNode } from '@/models/common';

const useCaseReviewStore = defineStore('caseReview', {
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
    async initModules(id: string) {
      try {
        this.loading = true;
        const res = await getReviewDetailModuleTree(id);
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
    async getModuleCount(params: ReviewDetailCaseListQueryParams) {
      try {
        this.modulesCount = await getReviewDetailModuleCount(params);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
  },
});

export default useCaseReviewStore;
