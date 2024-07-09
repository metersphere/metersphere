import { defineStore } from 'pinia';

import { getReviewDetailModuleCount } from '@/api/modules/case-management/caseReview';

import type { ReviewDetailCaseListQueryParams } from '@/models/caseManagement/caseReview';

const useCaseReviewStore = defineStore('caseReview', {
  state: (): {
    modulesCount: Record<string, any>; // 用例树模块数量
  } => ({
    modulesCount: {},
  }),
  actions: {
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
