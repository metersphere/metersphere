/* eslint-disable no-shadow */
import { getCaseModulesCounts, getPublicLinkCaseModulesCounts } from '@/api/modules/case-management/featureCase';

export enum RequestModuleEnum {
  API_CASE = 'API_CASE',
  CASE_MANAGEMENT = 'CASE_MANAGEMENT',
}

export function initGetModuleCountFunc(type: RequestModuleEnum[keyof RequestModuleEnum], params: Record<string, any>) {
  switch (type) {
    case RequestModuleEnum.API_CASE:
      return getPublicLinkCaseModulesCounts(params);
    case RequestModuleEnum.CASE_MANAGEMENT:
      return getCaseModulesCounts(params);
    default:
      break;
  }
}

export default {};
