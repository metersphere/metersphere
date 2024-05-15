import type { MsTableColumnData } from '@/components/pure/ms-table/type';

import { useI18n } from '@/hooks/useI18n';
import { useAppStore } from '@/store';
import useLicenseStore from '@/store/modules/setting/license';
import { hasAnyPermission } from '@/utils/permission';

import { TableKeyEnum } from '@/enums/tableEnum';
import { FilterRemoteMethodsEnum } from '@/enums/tableFilterEnum';
import type { ResourceTypeMapKey } from '@/enums/taskCenter';
import { TaskCenterEnum } from '@/enums/taskCenter';

const appStore = useAppStore();
const licenseStore = useLicenseStore();
const { t } = useI18n();
export const TaskStatus: Record<ResourceTypeMapKey, Record<string, { icon: string; label: string; color?: string }>> = {
  [TaskCenterEnum.API_CASE]: {
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: t('project.taskCenter.successful'),
    },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: t('project.taskCenter.failure'),
    },
    FAKE_ERROR: {
      icon: 'icon-icon_warning_colorful',
      label: t('project.taskCenter.falseAlarm'),
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: t('project.taskCenter.stop'),
      color: '!var(--color-text-input-border)',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: t('project.taskCenter.inExecution'),
      color: '!text-[rgb(var(--link-6))]',
    },
    // RERUNNING: {
    //   icon: 'icon-icon_testing',
    //   label: t('project.taskCenter.rerun',
    //   color: '!text-[rgb(var(--link-6))]',
    // },
    PENDING: {
      icon: 'icon-icon_wait',
      label: t('project.taskCenter.queuing'),
      color: '!text-[rgb(var(--link-6))]',
    },
  },
  [TaskCenterEnum.API_SCENARIO]: {
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: t('project.taskCenter.successful'),
    },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: t('project.taskCenter.failure'),
    },
    FAKE_ERROR: {
      icon: 'icon-icon_warning_colorful',
      label: t('project.taskCenter.falseAlarm'),
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: t('project.taskCenter.stop'),
      color: 'var(--color-text-input-border)',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: t('project.taskCenter.inExecution'),
      color: '!text-[rgb(var(--link-6))]',
    },
    // RERUNNING: {
    //   icon: 'icon-icon_testing',
    //   label: t('project.taskCenter.rerun',
    //   color: '!text-[rgb(var(--link-6))]',
    // },
    PENDING: {
      icon: 'icon-icon_wait',
      label: t('project.taskCenter.queuing'),
      color: '!text-[rgb(var(--link-6))]',
    },
  },
  [TaskCenterEnum.LOAD_TEST]: {
    STARTING: {
      icon: 'icon-icon_restarting',
      label: t('project.taskCenter.starting'),
      color: '!text-[rgb(var(--link-6))]',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: t('project.taskCenter.inExecution'),
      color: '!text-[rgb(var(--link-6))]',
    },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: t('project.taskCenter.failure'),
    },
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: t('project.taskCenter.successful'),
    },
    COMPLETED: {
      icon: 'icon-icon_succeed_colorful',
      label: t('project.taskCenter.complete'),
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: t('project.taskCenter.stop'),
      color: 'var(--color-text-input-border)',
    },
  },
  [TaskCenterEnum.UI_TEST]: {
    PENDING: {
      icon: 'icon-icon_wait',
      label: t('project.taskCenter.queuing'),
      color: '!text-[rgb(var(--link-6))]',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: t('project.taskCenter.inExecution'),
      color: '!text-[rgb(var(--link-6))]',
    },
    // RERUNNING: {
    //   icon: 'icon-icon_testing',
    //   label: t('project.taskCenter.rerun',
    //   color: '!text-[rgb(var(--link-6))]',
    // },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: t('project.taskCenter.failure'),
    },
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: t('project.taskCenter.successful'),
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: t('project.taskCenter.stop'),
      color: 'var(--color-text-input-border)',
    },
  },
  [TaskCenterEnum.TEST_PLAN]: {
    RUNNING: {
      icon: 'icon-icon_testing',
      label: t('project.taskCenter.queuing'),
      color: '!text-[rgb(var(--link-6))]',
    },
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: t('project.taskCenter.successful'),
    },
    STARTING: {
      icon: 'icon-icon_restarting',
      label: t('project.taskCenter.starting'),
      color: '!text-[rgb(var(--link-6))]',
    },
  },
};

export type Group = 'system' | 'organization' | 'project';

export type ExtractedKeys = Extract<ResourceTypeMapKey, TaskCenterEnum.API_CASE | TaskCenterEnum.API_SCENARIO>;

export const resourceTypeMap: Record<ResourceTypeMapKey, Record<string, any>> = {
  [TaskCenterEnum.API_CASE]: {
    value: TaskCenterEnum.API_CASE,
    label: t('project.taskCenter.interfaceCase'),
  },
  [TaskCenterEnum.API_SCENARIO]: {
    value: TaskCenterEnum.API_SCENARIO,
    label: t('project.taskCenter.apiScenario'),
  },
  [TaskCenterEnum.UI_TEST]: {
    value: TaskCenterEnum.UI_TEST,
    label: t('project.taskCenter.uiDefaultFile'),
  },
  [TaskCenterEnum.LOAD_TEST]: {
    value: TaskCenterEnum.LOAD_TEST,
    label: t('project.taskCenter.performanceTest'),
  },
  [TaskCenterEnum.TEST_PLAN]: {
    value: TaskCenterEnum.TEST_PLAN,
    label: t('project.taskCenter.testPlan'),
  },
};

export function getOrgColumns(): MsTableColumnData {
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
  const systemKey = [
    TableKeyEnum.TASK_API_CASE_SYSTEM,
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

export default {};
