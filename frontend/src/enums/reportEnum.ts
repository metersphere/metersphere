// 模板展示字段icon
import { useI18n } from '@/hooks/useI18n';

const { t } = useI18n();
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

export const ReportStatus: Record<ReportEnum, Record<string, { icon: string; label: string; color?: string }>> = {
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
      label: 'report.fake.error',
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: 'report.stopped',
      color: '!var(--color-text-input-border)',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: 'report.status.running',
      color: '!text-[rgb(var(--link-6))]',
    },
    // RERUNNING: {
    //   icon: 'icon-icon_testing',
    //   label: 'report.rerun',
    //   color: '!text-[rgb(var(--link-6))]',
    // },
    PENDING: {
      icon: 'icon-icon_wait',
      label: 'report.status.pending',
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
      label: 'report.fake.error',
    },
    STOPPED: {
      icon: 'icon-icon_block_filled',
      label: 'report.stopped',
      color: 'var(--color-text-input-border)',
    },
    RUNNING: {
      icon: 'icon-icon_testing',
      label: 'report.status.running',
      color: '!text-[rgb(var(--link-6))]',
    },
    // RERUNNING: {
    //   icon: 'icon-icon_testing',
    //   label: 'report.rerun',
    //   color: '!text-[rgb(var(--link-6))]',
    // },
    PENDING: {
      icon: 'icon-icon_wait',
      label: 'report.status.pending',
      color: '!text-[rgb(var(--link-6))]',
    },
  },
};

export const PlanReportStatus: Record<string, any> = {
  [ReportStatusEnum.EXEC_STATUS]: {
    STOPPED: {
      key: 'STOPPED',
      icon: 'icon-icon_block_filled',
      statusText: t('report.stopped'),
      label: 'report.stop',
      color: '!var(--color-text-input-border)',
    },
    RUNNING: {
      key: 'RUNNING',
      icon: 'icon-icon_testing',
      statusText: t('report.status.running'),
      label: 'report.inExecution',
      color: '!text-[rgb(var(--link-6))]',
    },
    PENDING: {
      key: 'PENDING',
      icon: 'icon-icon_wait',
      statusText: t('report.status.pending'),
      label: 'report.queuing',
      color: '!text-[rgb(var(--link-6))]',
    },
    COMPLETED: {
      key: 'COMPLETED',
      icon: 'icon-icon_succeed_colorful',
      statusText: t('report.completed'),
      label: 'report.successful',
    },
  },
  [ReportStatusEnum.REPORT_STATUS]: {
    SUCCESS: {
      icon: 'icon-icon_succeed_colorful',
      label: 'report.successful',
    },
    ERROR: {
      icon: 'icon-icon_close_colorful',
      label: 'report.failure',
    },
  },
};

export default {};
