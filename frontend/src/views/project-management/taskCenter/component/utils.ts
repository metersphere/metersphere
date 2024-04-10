import { TaskCenterEnum } from '@/enums/taskCenter';

export const TaskStatus = {
  [TaskCenterEnum.API_CASE]: {
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: 'project.taskCenter.successful',
    },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: 'project.taskCenter.failure',
    },
    FAKE_ERROR: {
      icon: 'icon-icon_warning_colorful',
      label: 'project.taskCenter.falseAlarm',
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: 'project.taskCenter.stop',
      color: '!var(--color-text-input-border)',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: 'project.taskCenter.inExecution',
      color: '!text-[rgb(var(--link-6))]',
    },
    // RERUNNING: {
    //   icon: 'icon-icon_testing',
    //   label: 'project.taskCenter.rerun',
    //   color: '!text-[rgb(var(--link-6))]',
    // },
    PENDING: {
      icon: 'icon-icon_wait',
      label: 'project.taskCenter.queuing',
      color: '!text-[rgb(var(--link-6))]',
    },
  },
  [TaskCenterEnum.API_SCENARIO]: {
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: 'project.taskCenter.successful',
    },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: 'project.taskCenter.failure',
    },
    FAKE_ERROR: {
      icon: 'icon-icon_warning_colorful',
      label: 'project.taskCenter.falseAlarm',
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: 'project.taskCenter.stop',
      color: 'var(--color-text-input-border)',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: 'project.taskCenter.inExecution',
      color: '!text-[rgb(var(--link-6))]',
    },
    // RERUNNING: {
    //   icon: 'icon-icon_testing',
    //   label: 'project.taskCenter.rerun',
    //   color: '!text-[rgb(var(--link-6))]',
    // },
    PENDING: {
      icon: 'icon-icon_wait',
      label: 'project.taskCenter.queuing',
      color: '!text-[rgb(var(--link-6))]',
    },
  },
  [TaskCenterEnum.LOAD_TEST]: {
    STARTING: {
      icon: 'icon-icon_restarting',
      label: 'project.taskCenter.starting',
      color: '!text-[rgb(var(--link-6))]',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: 'project.taskCenter.inExecution',
      color: '!text-[rgb(var(--link-6))]',
    },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: 'project.taskCenter.failure',
    },
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: 'project.taskCenter.successful',
    },
    COMPLETED: {
      icon: 'icon-icon_succeed_colorful',
      label: 'project.taskCenter.complete',
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: 'project.taskCenter.stop',
      color: 'var(--color-text-input-border)',
    },
  },
  [TaskCenterEnum.UI_TEST]: {
    PENDING: {
      icon: 'icon-icon_wait',
      label: 'project.taskCenter.queuing',
      color: '!text-[rgb(var(--link-6))]',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: 'project.taskCenter.inExecution',
      color: '!text-[rgb(var(--link-6))]',
    },
    // RERUNNING: {
    //   icon: 'icon-icon_testing',
    //   label: 'project.taskCenter.rerun',
    //   color: '!text-[rgb(var(--link-6))]',
    // },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: 'project.taskCenter.failure',
    },
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: 'project.taskCenter.successful',
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: 'project.taskCenter.stop',
      color: 'var(--color-text-input-border)',
    },
  },
  [TaskCenterEnum.TEST_PLAN]: {
    RUNNING: {
      icon: 'icon-icon_testing',
      label: 'project.taskCenter.queuing',
      color: '!text-[rgb(var(--link-6))]',
    },
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: 'project.taskCenter.successful',
    },
    STARTING: {
      icon: 'icon-icon_restarting',
      label: 'project.taskCenter.starting',
      color: '!text-[rgb(var(--link-6))]',
    },
  },
};

export const resourceTypeMap = {
  [TaskCenterEnum.API_CASE]: {
    value: TaskCenterEnum.API_CASE,
    label: 'project.taskCenter.interfaceCase',
  },
  [TaskCenterEnum.API_SCENARIO]: {
    value: TaskCenterEnum.API_SCENARIO,
    label: 'project.taskCenter.apiScenario',
  },
  [TaskCenterEnum.UI_TEST]: {
    value: TaskCenterEnum.UI_TEST,
    label: 'project.taskCenter.uiDefaultFile',
  },
  [TaskCenterEnum.LOAD_TEST]: {
    value: TaskCenterEnum.LOAD_TEST,
    label: 'project.taskCenter.performanceTest',
  },
  [TaskCenterEnum.TEST_PLAN]: {
    value: TaskCenterEnum.TEST_PLAN,
    label: 'project.taskCenter.testPlan',
  },
};

export default {};
