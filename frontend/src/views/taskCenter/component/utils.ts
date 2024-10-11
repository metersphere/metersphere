import type { MsTableColumnData } from '@/components/pure/ms-table/type';

import { useI18n } from '@/hooks/useI18n';
import { useAppStore } from '@/store';
import useLicenseStore from '@/store/modules/setting/license';
import { hasAnyPermission } from '@/utils/permission';

import { TableKeyEnum } from '@/enums/tableEnum';
import { FilterRemoteMethodsEnum } from '@/enums/tableFilterEnum';

const { t } = useI18n();

// 执行结果
export const executeResultMap: Record<string, any> = {
  SUCCESS: {
    icon: 'icon-icon_succeed_colorful',
    label: 'common.success',
    color: 'rgb(var(--success-6))',
  },
  ERROR: {
    icon: 'icon-icon_close_colorful',
    label: 'common.fail',
    color: 'rgb(var(--danger-6))',
  },
  FAKE_ERROR: {
    icon: 'icon-icon_warning_colorful',
    label: 'common.fakeError',
    color: 'rgb(var(--warning-6))',
  },
};

// 执行完成率
export const executeFinishedRateMap: Record<string, any> = {
  SUCCESS: {
    label: 'common.success',
    color: 'rgb(var(--success-6))',
  },
  ERROR: {
    label: 'common.fail',
    color: 'rgb(var(--danger-6))',
  },
  FAKE_ERROR: {
    label: 'common.fakeError',
    color: 'rgb(var(--warning-6))',
  },
  BLOCK: {
    label: 'common.block',
    color: 'rgb(179,121,200)',
  },
  UN_EXECUTE: {
    label: 'common.unExecute',
    color: 'var(--color-text-4)',
  },
};

// 执行状态
export const executeStatusMap: Record<string, any> = {
  PENDING: {
    label: 'common.unExecute',
    color: 'var(--color-text-n8)',
    class: '!text-[var(--color-text-1)]',
  },
  RUNNING: {
    label: 'common.running',
    color: 'rgb(var(--link-2))',
    class: '!text-[rgb(var(--link-6))]',
  },
  STOPPED: {
    label: 'common.stopped',
    color: 'rgb(var(--warning-2))',
    class: '!text-[rgb(var(--warning-6))]',
  },
  COMPLETED: {
    label: 'common.completed',
    color: 'rgb(var(--success-2))',
    class: '!text-[rgb(var(--success-6))]',
  },
  RERUNNING: {
    label: 'ms.taskCenter.failRerun',
    color: 'rgb(var(--link-2))',
    class: '!text-[rgb(var(--link-6))]',
  },
};

// 执行方式
export const executeMethodMap: Record<string, any> = {
  MANUAL: 'ms.taskCenter.execute',
  BATCH: 'ms.taskCenter.batchExecute',
  API: 'ms.taskCenter.interfaceCall',
  SCHEDULE: 'ms.taskCenter.scheduledTask',
};

export type Group = 'system' | 'organization' | 'project';

export function getOrgColumns(): MsTableColumnData {
  const licenseStore = useLicenseStore();
  const config: MsTableColumnData = {
    title: 'project.belongOrganization',
    dataIndex: 'organizationIds',
    slotName: 'organizationName',
    showDrag: true,
    width: 200,
    showInTable: true,
  };
  if (licenseStore.hasLicense()) {
    config.filterConfig = {
      mode: 'remote',
      remoteMethod: FilterRemoteMethodsEnum.SYSTEM_ORGANIZATION_LIST,
      placeholderText: t('project.taskCenter.filterOrgPlaceholderText'),
    };
  }
  return config;
}

export function getProjectColumns(key: TableKeyEnum): MsTableColumnData {
  const appStore = useAppStore();
  const systemKey = [
    TableKeyEnum.TASK_CENTER_CASE_TASK,
    TableKeyEnum.TASK_SCHEDULE_TASK_API_IMPORT_SYSTEM,
    TableKeyEnum.TASK_SCHEDULE_TASK_API_SCENARIO_SYSTEM,
  ];
  const filterKeyPermission = systemKey.includes(key)
    ? hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ'])
    : hasAnyPermission(['ORGANIZATION_PROJECT:READ']);
  const remoteMethod = systemKey.includes(key)
    ? FilterRemoteMethodsEnum.SYSTEM_PROJECT_LIST
    : FilterRemoteMethodsEnum.SYSTEM_ORGANIZATION_PROJECT;

  const config: MsTableColumnData = {
    title: 'project.belongProject',
    dataIndex: 'projectIds',
    slotName: 'projectName',
    showDrag: true,
    width: 200,
    showInTable: true,
  };
  if (filterKeyPermission && remoteMethod) {
    config.filterConfig = {
      mode: 'remote',
      loadOptionParams: {
        organizationId: appStore.currentOrgId,
      },
      remoteMethod,
      placeholderText: t('project.taskCenter.filterProPlaceholderText'),
    };
  }
  return config;
}
