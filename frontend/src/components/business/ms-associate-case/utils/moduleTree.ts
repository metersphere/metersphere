import { getModuleTreeOnlyModules } from '@/api/modules/api-test/management';
import { getModuleTree as getScenarioModuleTree } from '@/api/modules/api-test/scenario';
import { getCaseModuleTree } from '@/api/modules/case-management/featureCase';

import { CaseModulesApiTypeEnum } from '@/enums/associateCaseEnum';
import { CaseLinkEnum } from '@/enums/caseEnum';

// 模块树接口
export const getModuleTreeApiMap: Record<string, any> = {
  [CaseModulesApiTypeEnum.TEST_PLAN_LINK_CASE_MODULE]: {
    [CaseLinkEnum.FUNCTIONAL]: getCaseModuleTree,
    [CaseLinkEnum.API]: getModuleTreeOnlyModules,
    [CaseLinkEnum.SCENARIO]: getScenarioModuleTree,
  },
};

// 获取关联用例模块
export function getModuleTreeFunc(
  getModulesApiType: CaseModulesApiTypeEnum[keyof CaseModulesApiTypeEnum],
  activeTab: keyof typeof CaseLinkEnum,
  params: Record<string, any>
) {
  switch (getModulesApiType) {
    case CaseModulesApiTypeEnum.TEST_PLAN_LINK_CASE_MODULE:
      return getModuleTreeApiMap[getModulesApiType][activeTab](params);
    default:
      break;
  }
}

export default {};
