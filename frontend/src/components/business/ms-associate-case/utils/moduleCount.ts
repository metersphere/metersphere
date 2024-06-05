import { getModuleCount } from '@/api/modules/api-test/management';
import { getModuleCount as getScenarioModuleCount } from '@/api/modules/api-test/scenario';
import { getCaseModulesCounts } from '@/api/modules/case-management/featureCase';

import { CaseCountApiTypeEnum } from '@/enums/associateCaseEnum';
import { CaseLinkEnum } from '@/enums/caseEnum';

// 获取模块数量Map
export const getModuleTreeCountApiMap: Record<string, any> = {
  [CaseCountApiTypeEnum.TEST_PLAN_CASE_COUNT]: {
    [CaseLinkEnum.FUNCTIONAL]: getCaseModulesCounts,
    [CaseLinkEnum.API]: getModuleCount,
    [CaseLinkEnum.SCENARIO]: getScenarioModuleCount,
  },
};

// 获取模块count
export function initGetModuleCountFunc(
  type: CaseCountApiTypeEnum[keyof CaseCountApiTypeEnum],
  activeTab: keyof typeof CaseLinkEnum,
  params: Record<string, any>
) {
  switch (type) {
    case CaseCountApiTypeEnum.TEST_PLAN_CASE_COUNT:
      return getModuleTreeCountApiMap[type][activeTab](params);
    default:
      break;
  }
}

export default {};
