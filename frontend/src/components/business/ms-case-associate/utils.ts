/* eslint-disable no-shadow */
import { getModuleTreeCounts } from '@/api/modules/bug-management';
import { getCaseModulesCounts, getPublicLinkCaseModulesCounts } from '@/api/modules/case-management/featureCase';

import { StatusType } from '@/enums/caseEnum';

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

export const lastExecuteResultMap: Record<string, any> = {
  PENDING: {
    label: 'PENDING',
    icon: StatusType.PENDING,
    statusText: 'common.unExecute',
    color: 'var(--color-text-brand)',
  },
  SUCCESS: {
    label: 'SUCCESS',
    icon: StatusType.SUCCESS,
    statusText: 'common.success',
    color: '',
  },
  BLOCKED: {
    label: 'BLOCKED',
    icon: StatusType.BLOCKED,
    statusText: 'common.block',
    color: 'var(--color-fill-p-3)',
  },
  ERROR: {
    label: 'ERROR',
    icon: StatusType.ERROR,
    statusText: 'common.fail',
    color: '',
  },
  FAKE_ERROR: {
    icon: 'icon-icon_warning_colorful',
    statusText: 'common.fakeError',
    label: 'FAKE_ERROR',
    color: '',
  },
};
