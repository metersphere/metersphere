import { getUnAssociatedList } from '@/api/modules/bug-management';
import { getCaseList, getPublicLinkCaseList } from '@/api/modules/case-management/featureCase';
import {
  getPlanScenarioAssociatedList,
  getTestPlanAssociationApiList,
  getTestPlanAssociationCaseList,
  getTestPlanCaseList,
} from '@/api/modules/test-plan/testPlan';

import { CasePageApiTypeEnum } from '@/enums/associateCaseEnum';
import { CaseLinkEnum } from '@/enums/caseEnum';

// table接口模块定义
export const getPublicLinkCaseListMap: Record<string, any> = {
  // 功能用例 目前只有接口用例、场景用例
  [CasePageApiTypeEnum.FUNCTIONAL_CASE_PAGE]: {
    [CaseLinkEnum.FUNCTIONAL]: getPublicLinkCaseList,
    [CaseLinkEnum.API]: {
      API: getPublicLinkCaseList,
      CASE: getPublicLinkCaseList,
    },
  },
  // 用例评审 目前只有功能用例
  [CasePageApiTypeEnum.CASE_REVIEW_CASE_PAGE]: {
    [CaseLinkEnum.FUNCTIONAL]: getCaseList,
  },
  // 缺陷管理 目前只有功能用例
  [CasePageApiTypeEnum.BUG_MANAGEMENT_CASE_PAGE]: {
    [CaseLinkEnum.FUNCTIONAL]: getUnAssociatedList,
  },
  // 测试计划 目前有功能用例、接口用例、场景用例
  [CasePageApiTypeEnum.TEST_PLAN_CASE_PAGE]: {
    [CaseLinkEnum.FUNCTIONAL]: getTestPlanCaseList,
    [CaseLinkEnum.API]: {
      API: getTestPlanAssociationApiList,
      CASE: getTestPlanAssociationCaseList,
    },
    [CaseLinkEnum.SCENARIO]: getPlanScenarioAssociatedList,
  },
};

export default {};
