import { getModuleCount } from '@/api/modules/api-test/management';
import { getModuleCount as getScenarioModuleCount } from '@/api/modules/api-test/scenario';
import { getCaseModulesCounts, getPublicLinkCaseModulesCounts } from '@/api/modules/case-management/featureCase';
import { testPlanAssociateModuleCount } from '@/api/modules/test-plan/testPlan';

import { CaseCountApiTypeEnum } from '@/enums/associateCaseEnum';
import { CaseLinkEnum } from '@/enums/caseEnum';

// 获取模块数量Map
export const getModuleTreeCountApiMap: Record<string, any> = {
  [CaseCountApiTypeEnum.FUNCTIONAL_CASE_COUNT]: {
    [CaseLinkEnum.FUNCTIONAL]: getPublicLinkCaseModulesCounts,
    [CaseLinkEnum.API]: getPublicLinkCaseModulesCounts,
  },
  [CaseCountApiTypeEnum.TEST_PLAN_CASE_COUNT]: {
    [CaseLinkEnum.FUNCTIONAL]: getCaseModulesCounts,

    [CaseLinkEnum.API]: {
      API: getModuleCount,
      CASE: testPlanAssociateModuleCount,
    },
    [CaseLinkEnum.SCENARIO]: getScenarioModuleCount,
  },
};

// 获取模块count
export function initGetModuleCountFunc(
  type: CaseCountApiTypeEnum[keyof CaseCountApiTypeEnum],
  activeTab: keyof typeof CaseLinkEnum,
  showType: 'API' | 'CASE',
  params: Record<string, any>
) {
  if (activeTab !== CaseLinkEnum.API) {
    return getModuleTreeCountApiMap[type as keyof typeof CaseCountApiTypeEnum][activeTab](params);
  }
  return getModuleTreeCountApiMap[type as keyof typeof CaseCountApiTypeEnum][activeTab][showType](params);
}

export default {};
