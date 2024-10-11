// 模板展示字段icon

export enum ReportEnum {
  API_SCENARIO_REPORT = 'API_SCENARIO_REPORT',
  API_REPORT = 'API_REPORT',
}

export enum ReportStatusEnum {
  EXEC_STATUS = 'EXEC_STATUS',
  REPORT_STATUS = 'REPORT_STATUS',
}

export enum TriggerModeLabel {
  MANUAL = 'report.trigger.manual', // 手动执行
  SCHEDULE = 'report.trigger.scheduled', // 定时任务
  BATCH = 'report.trigger.batch.execution', // 批量执行
  API = 'report.trigger.interface', // 接口调用
}
export enum TriggerModeLabelEnum {
  MANUAL = 'MANUAL', // 手动执行
  SCHEDULE = 'SCHEDULE', // 定时任务
  BATCH = 'BATCH', // 批量执行
  API = 'API', // 接口调用
}

export const ReportStatus: Record<string, any> = {
  SUCCESS: {
    icon: 'icon-icon_succeed_colorful',
    label: 'common.success',
  },
  ERROR: {
    icon: 'icon-icon_close_colorful',
    label: 'common.fail',
  },
  FAKE_ERROR: {
    icon: 'icon-icon_warning_colorful',
    label: 'common.fakeError',
  },
};

export const PlanReportStatus: Record<string, any> = {
  SUCCESS: {
    icon: 'icon-icon_succeed_colorful',
    label: 'common.success',
  },
  ERROR: {
    icon: 'icon-icon_close_colorful',
    label: 'common.fail',
  },
};

export default {};
