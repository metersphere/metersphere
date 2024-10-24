import type { ExecuteStatusEnum } from '@/enums/taskCenter';

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
export const executeStatusMap: Record<ExecuteStatusEnum, any> = {
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

// 系统后台任务类型
export const scheduleTaskTypeMap: Record<string, any> = {
  API_IMPORT: 'ms.taskCenter.apiImport',
  API_SCENARIO: 'ms.taskCenter.apiScenario',
  BUG_SYNC: 'ms.taskCenter.thirdPartSync',
  DEMAND_SYNC: 'ms.taskCenter.thirdPartSync',
  TEST_PLAN: 'menu.testPlan',
  TEST_PLAN_GROUP: 'menu.testPlanGroup',
};
