<template>
  <div class="flex items-center justify-start">
    <MsIcon :type="getExecutionResult().icon" :class="getExecutionResult()?.color" size="14" />
    <span class="ml-1">{{ t(getExecutionResult().label) }}</span>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ReportStatusEnum } from '@/enums/reportEnum';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
    moduleType: keyof typeof ReportStatusEnum;
  }>();

  export interface IconType {
    icon: string;
    label: string;
    color?: string;
  }

  const iconTypeStatus: Record<string, any> = {
    [ReportStatusEnum.REPORT_STATUS]: {
      SUCCESS: {
        icon: 'icon-icon_succeed_colorful',
        label: 'common.success',
      },
      ERROR: {
        icon: 'icon-icon_close_colorful',
        label: 'common.fail',
      },
      DEFAULT: {
        label: '-',
        color: '!text-[var(--color-text-input-border)]',
      },
    },
    [ReportStatusEnum.EXEC_STATUS]: {
      STOPPED: {
        icon: 'icon-icon_block_filled',
        label: 'common.stop',
        color: '!text-[var(--color-text-input-border)]',
      },
      RUNNING: {
        icon: 'icon-icon_testing',
        label: 'common.running',
        color: '!text-[rgb(var(--link-6))]',
      },
      PENDING: {
        icon: 'icon-icon_wait',
        label: 'common.unExecute',
        color: '!text-[var(--color-text-input-border)]',
      },
      COMPLETED: {
        icon: 'icon-icon_wait',
        label: 'report.completed',
        color: '!text-[var(--color-text-input-border)]',
      },
    },
  };

  function getExecutionResult(): IconType {
    const moduleConfig = iconTypeStatus[props.moduleType];
    return moduleConfig?.[props.status] ?? moduleConfig?.DEFAULT;
  }
</script>

<style scoped></style>
