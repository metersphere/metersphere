/* eslint-disable no-shadow */
import { getModuleTreeCounts } from '@/api/modules/bug-management';
import { getCaseModulesCounts, getPublicLinkCaseModulesCounts } from '@/api/modules/case-management/featureCase';

export enum RequestModuleEnum {
  API_CASE = 'API_CASE',
  CASE_MANAGEMENT = 'CASE_MANAGEMENT',
  BUG_MANAGEMENT = 'BUG_MANAGEMENT',
}

export function initGetModuleCountFunc(type: RequestModuleEnum[keyof RequestModuleEnum], params: Record<string, any>) {
  switch (type) {
    case RequestModuleEnum.API_CASE:
      return getPublicLinkCaseModulesCounts(params);
    case RequestModuleEnum.CASE_MANAGEMENT:
      return getCaseModulesCounts(params);
    case RequestModuleEnum.BUG_MANAGEMENT:
      return getModuleTreeCounts(params);
    default:
      break;
  }
}

export default {};
