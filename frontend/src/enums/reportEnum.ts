// 模板展示字段icon
export enum ReportEnum {
  API_SCENARIO_REPORT = 'API_SCENARIO_REPORT',
  API_REPORT = 'API_REPORT',
}

export enum TriggerModeLabel {
  MANUAL = 'report.trigger.manual', // 手动执行
  SCHEDULE = 'report.trigger.scheduled', // 定时任务
  BATCH = 'report.trigger.batch.execution', // 批量执行
  API = 'report.trigger.interface', // 接口调用
}

export const ReportStatus = {
  [ReportEnum.API_REPORT]: {
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: 'report.successful',
    },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: 'report.failure',
    },
    FAKE_ERROR: {
      icon: 'icon-icon_warning_colorful',
      label: 'report.falseAlarm',
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: 'report.stop',
      color: '!var(--color-text-input-border)',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: 'report.inExecution',
      color: '!text-[rgb(var(--link-6))]',
    },
    // RERUNNING: {
    //   icon: 'icon-icon_testing',
    //   label: 'report.rerun',
    //   color: '!text-[rgb(var(--link-6))]',
    // },
    PENDING: {
      icon: 'icon-icon_wait',
      label: 'report.queuing',
      color: '!text-[rgb(var(--link-6))]',
    },
  },
  [ReportEnum.API_SCENARIO_REPORT]: {
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: 'report.successful',
    },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: 'report.failure',
    },
    FAKE_ERROR: {
      icon: 'icon-icon_warning_colorful',
      label: 'report.falseAlarm',
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: 'report.stop',
      color: 'var(--color-text-input-border)',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: 'report.inExecution',
      color: '!text-[rgb(var(--link-6))]',
    },
    // RERUNNING: {
    //   icon: 'icon-icon_testing',
    //   label: 'report.rerun',
    //   color: '!text-[rgb(var(--link-6))]',
    // },
    PENDING: {
      icon: 'icon-icon_wait',
      label: 'report.queuing',
      color: '!text-[rgb(var(--link-6))]',
    },
  },
};
export default {};
